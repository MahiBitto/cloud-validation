package com.crypto.keymaker.listener;

import com.crypto.keymaker.dao.TransactionAuditDao;
import com.crypto.keymaker.dto.OnboardRequest;
import com.crypto.keymaker.entity.AdminRequest;
import com.crypto.keymaker.entity.AdminResp;
import com.crypto.keymaker.scheduler.OperationalGovernanceSchedulers;
import com.crypto.keymaker.service.OnboardingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.pubsub.v1.PubsubMessage;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import jakarta.annotation.PostConstruct;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Component
public class ThrottledPubSubListener implements Consumer<BasicAcknowledgeablePubsubMessage> {

    private static final Logger log = LoggerFactory.getLogger(ThrottledPubSubListener.class);

    private final OnboardingService onboardingService;
    private final OperationalGovernanceSchedulers governance;
    private final TransactionAuditDao auditDao;
    private final ObjectMapper mapper;
    private final PubSubTemplate pubSubTemplate;
    private final Tracer tracer; // Injected for manual telemetry orchestration

    @Value("${keymaker.subscription-name:keymaker-inbound-sub}") private String subscriptionName;
    @Value("${keymaker.response-topic:uac_events_response}") private String responseTopicName;

    public ThrottledPubSubListener(OnboardingService onboardingService, 
                                   OperationalGovernanceSchedulers governance, 
                                   TransactionAuditDao auditDao, ObjectMapper mapper, 
                                   PubSubTemplate pubSubTemplate, Tracer tracer) {
        this.onboardingService = onboardingService;
        this.governance = governance;
        this.auditDao = auditDao;
        this.mapper = mapper;
        this.pubSubTemplate = pubSubTemplate;
        this.tracer = tracer;
    }

    @PostConstruct
    public void startAsyncSubscriptionLoop() {
        try {
            pubSubTemplate.subscribe(subscriptionName, this);
            log.info("👉 Keymaker async message engine cleanly attached to subscription queue: {}", subscriptionName);
        } catch (Exception e) {
            log.error("⚠️ Pub/Sub listener injection failed: {}", e.getMessage(), e);
        }
    }

    @Override
    public void accept(BasicAcknowledgeablePubsubMessage acknowledgeableMessage) {
        PubsubMessage message = acknowledgeableMessage.getPubsubMessage();
        String corrId = message.getAttributesOrDefault("correlation_id", UUID.randomUUID().toString());
        String rawJsonPayload = message.getData().toStringUtf8();
        String currentRequestId = UUID.randomUUID().toString();

        // Step 1: Extract incoming distributed trace headers from message attributes if present
        Context extractedContext = extractTraceContext(message);
        
        // Step 2: Build a telemetry span linked directly to the parent context
        Span span = tracer.spanBuilder("Keymaker.PubSubConsumer")
                .setParent(extractedContext)
                .startSpan();
        span.setAttribute("correlation_id", corrId);
        span.setAttribute("request_id", currentRequestId);

        // Scope boundary ensures downstream DB executions (DAOs) inherit this trace context automatically
        try (Scope scope = span.makeCurrent()) {
            
            if (governance.isPipelineThrottled()) {
                log.warn("[Throttler Active] 12h limit hit. NACK message: {}", corrId);
                span.addEvent("Pipeline throttled due to 12-hour volume limit.");
                acknowledgeableMessage.nack();
                return;
            }

            OnboardRequest parsedRequest = mapper.readValue(rawJsonPayload, OnboardRequest.class);
            span.setAttribute("safename", parsedRequest.safename());
            span.setAttribute("key_type", parsedRequest.key_type());

            // Audit log incoming request (Auto-commit mode)
            AdminRequest auditReq = new AdminRequest();
            auditReq.setRequestId(currentRequestId);
            auditReq.setCorrelationId(corrId);
            auditReq.setSafename(parsedRequest.safename());
            auditReq.setKeyname(parsedRequest.keyname());
            auditReq.setKeyType(parsedRequest.key_type());
            auditReq.setPayload(rawJsonPayload);
            auditReq.setReceivedTs(Instant.now());
            auditDao.logIncomingRequest(auditReq);
            
            // Execute onboarding business logic (Inherits trace automatically)
            String resourceId = onboardingService.executeOnboard(parsedRequest, corrId);

            // Audit Log Success
            AdminResp successResp = new AdminResp();
            successResp.setResponseId(UUID.randomUUID().toString());
            successResp.setRequestId(currentRequestId);
            successResp.setCorrelationId(corrId);
            successResp.setStatus("SUCCESS");
            successResp.setPublishedTs(Instant.now());
            auditDao.logOutboundResponse(successResp);

            publishEventResponse(corrId, "SUCCESS", Map.of("keyspec_id", resourceId, "message", "Onboarding completed successfully."));
            acknowledgeableMessage.ack();
            span.setStatus(io.opentelemetry.api.trace.StatusCode.OK, "Message processed and audited completely.");

        } catch (Exception e) {
            log.error("[Failure] Ingestion failed for tracing ID: {}", corrId, e);
            span.recordException(e);
            span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, e.getMessage());

            AdminResp failureResp = new AdminResp();
            failureResp.setResponseId(UUID.randomUUID().toString());
            failureResp.setRequestId(currentRequestId);
            failureResp.setCorrelationId(corrId);
            failureResp.setStatus("FAILED");
            failureResp.setErrorMessage(e.getMessage());
            failureResp.setPublishedTs(Instant.now());
            auditDao.logOutboundResponse(failureResp);

            publishEventResponse(corrId, "FAILED", Map.of("error", e.getMessage()));
            acknowledgeableMessage.nack();
        } finally {
            span.end(); // Commits the span metrics to the OTel exporter collector
        }
    }

    private Context extractTraceContext(PubsubMessage message) {
        String traceParent = message.getAttributesOrDefault("traceparent", null);
        if (traceParent == null || traceParent.isEmpty()) {
            return Context.current(); // Fallback to current local context if no parent is provided
        }

        try {
            // W3C Trace Context Standard format parsing: "00-traceId-spanId-traceFlags"
            String[] parts = traceParent.split("-");
            if (parts.length >= 4) {
                String traceId = parts[1];
                String spanId = parts[2];
                SpanContext remoteSpanContext = SpanContext.createFromRemoteParent(
                        traceId, spanId, TraceFlags.getSampled(), TraceState.getDefault());
                return Context.current().with(Span.wrap(remoteSpanContext));
            }
        } catch (Exception e) {
            log.warn("Failed to cleanly parse W3C traceparent attribute header framework: {}", e.getMessage());
        }
        return Context.current();
    }

    private void publishEventResponse(String correlationId, String status, Map<String, Object> bodyContent) {
        try {
            Map<String, Object> messageWrapper = Map.of(
                "correlation_id", correlationId,
                "status", status,
                "payload", bodyContent,
                "timestamp", Instant.now().toString()
            );
            String jsonOutput = mapper.writeValueAsString(messageWrapper);
            
            // Forward the active trace token to the outbound response channel so upstream systems can trace the entire lifecycle
            String traceParentValue = String.format("00-%s-%s-01", 
                    Span.current().getSpanContext().getTraceId(), 
                    Span.current().getSpanContext().getSpanId());

            pubSubTemplate.publish(responseTopicName, jsonOutput, Map.of(
                    "correlation_id", correlationId, 
                    "status", status,
                    "traceparent", traceParentValue));
        } catch (Exception ex) {
            log.error("Failed to broadcast telemetry response trace: {}", ex.getMessage());
        }
    }
}
