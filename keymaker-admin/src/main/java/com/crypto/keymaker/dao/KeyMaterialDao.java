package com.crypto.keymaker.dao;

import com.crypto.keymaker.entity.KeyMaterialInfo;
import com.crypto.keymaker.repository.KeyMaterialInfoRepository;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.stereotype.Component;

@Component
public class KeyMaterialDao {
    private final KeyMaterialInfoRepository repository;
    private final Tracer tracer;

    public KeyMaterialDao(KeyMaterialInfoRepository repository, Tracer tracer) {
        this.repository = repository;
        this.tracer = tracer;
    }

    public void persistCryptographicMaterial(KeyMaterialInfo materialInfo) {
        Span span = tracer.spanBuilder("SpannerDao.PersistCryptographicMaterial").startSpan();
        try { repository.save(materialInfo); } 
        finally { span.end(); }
    }
}
