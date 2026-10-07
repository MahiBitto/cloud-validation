package com.crypto.keymaker.entity;

import com.google.cloud.spring.data.spanner.core.mapping.Column;
import com.google.cloud.spring.data.spanner.core.mapping.Table;
import org.springframework.data.annotation.Id;
import java.time.Instant;

@Table(name = "admin_resp")
public class AdminResp {
    @Id @Column(name = "response_id") private String responseId;
    @Column(name = "request_id") private String requestId;
    @Column(name = "correlation_id") private String correlationId;
    private String status;
    @Column(name = "error_message") private String errorMessage;
    @Column(name = "published_ts") private Instant publishedTs;

    // Getters and Setters
    public String getResponseId() { return responseId; }
    public void setResponseId(String id) { this.responseId = id; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String id) { this.requestId = id; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String id) { this.correlationId = id; }
    public String getStatus() { return status; }
    public void setStatus(String stat) { this.status = stat; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String msg) { this.errorMessage = msg; }
    public Instant getPublishedTs() { return publishedTs; }
    public void setPublishedTs(Instant ts) { this.publishedTs = ts; }
}
