package com.crypto.keymaker.dao;

import com.crypto.keymaker.entity.KeyspecInfo;
import com.crypto.keymaker.repository.KeyspecInfoRepository;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.Optional;

@Component
public class KeyProfileDao {
    private final KeyspecInfoRepository repository;
    private final Tracer tracer;

    public KeyProfileDao(KeyspecInfoRepository repository, Tracer tracer) {
        this.repository = repository;
        this.tracer = tracer;
    }

    public Optional<KeyspecInfo> findActiveProfile(String safename, String keyname) {
        Span span = tracer.spanBuilder("SpannerDao.FindActiveProfile").startSpan();
        try { return repository.findBySafenameAndKeyname(safename, keyname); } 
        finally { span.end(); }
    }

    public KeyspecInfo saveProfile(KeyspecInfo profile) {
        Span span = tracer.spanBuilder("SpannerDao.SaveProfile").startSpan();
        try { return repository.save(profile); } 
        finally { span.end(); }
    }

    public long getCountCreatedWithinWindow(Instant startTime) {
        Span span = tracer.spanBuilder("SpannerDao.GetCountCreatedWithinWindow").startSpan();
        try { 
            //return repository.countGlobalKeysCreatedInWindow(startTime); 
            return repository.countByCreatTsGreaterThanEqual(startTime);
        } 
        finally { span.end(); }
    }
}
