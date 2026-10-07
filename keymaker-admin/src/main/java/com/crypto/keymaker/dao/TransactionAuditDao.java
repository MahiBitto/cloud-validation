package com.crypto.keymaker.dao;

import com.crypto.keymaker.entity.AdminRequest;
import com.crypto.keymaker.entity.AdminResp;
import com.crypto.keymaker.repository.AdminRequestRepository;
import com.crypto.keymaker.repository.AdminRespRepository;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransactionAuditDao {

    private final AdminRequestRepository reqRepository;
    private final AdminRespRepository respRepository;
    private final Tracer tracer;

    public TransactionAuditDao(AdminRequestRepository reqRepository, AdminRespRepository respRepository, Tracer tracer) {
        this.reqRepository = reqRepository;
        this.respRepository = respRepository;
        this.tracer = tracer;
    }

    // Propagation.REQUIRES_NEW ensures auditing records persist even if business logic later aborts or crashes
    //@Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logIncomingRequest(AdminRequest auditRequest) {
        Span span = tracer.spanBuilder("AuditDao.LogIncomingRequest").startSpan();
        try {
            reqRepository.save(auditRequest);
        } finally {
            span.end();
        }
    }

    //@Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logOutboundResponse(AdminResp auditResponse) {
        Span span = tracer.spanBuilder("AuditDao.LogOutboundResponse").startSpan();
        try {
            respRepository.save(auditResponse);
        } finally {
            span.end();
        }
    }
}
