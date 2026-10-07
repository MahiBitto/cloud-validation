package com.crypto.keymaker.dao.base;

import io.opentelemetry.api.trace.Tracer;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractSpannerDao {
    @Autowired
    protected Tracer tracer; // Automates tracing injection across all downstream database access operations
}