package com.crypto.keymaker.dao;

import com.crypto.keymaker.entity.KekInfo;
import com.crypto.keymaker.repository.KekInfoRepository;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class KekPoolDao {
    private final KekInfoRepository repository;
    private final Tracer tracer;

    public KekPoolDao(KekInfoRepository repository, Tracer tracer) {
        this.repository = repository;
        this.tracer = tracer;
    }

    public Optional<KekInfo> findAvailableKekForWrapping(String keyType) {
        Span span = tracer.spanBuilder("SpannerDao.FindAvailableKekForWrapping").startSpan();
        try { return repository.findAvailableKek(keyType); } 
        finally { span.end(); }
    }

    public KekInfo saveKek(KekInfo kek) {
        Span span = tracer.spanBuilder("SpannerDao.SaveKek").startSpan();
        try { return repository.save(kek); } 
        finally { span.end(); }
    }

    public long getActivePoolCushionSize(String keyType) {
        Span span = tracer.spanBuilder("SpannerDao.GetActivePoolCushionSize").startSpan();
        try { return repository.countAvailablePoolKeks(keyType); } 
        finally { span.end(); }
    }
}
