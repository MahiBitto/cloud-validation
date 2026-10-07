package com.example.keymaker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "keymaker.gcp")
public record KeymakerGcpProperties(
        String projectId,
        SpannerProperties spanner
) {

    public record SpannerProperties(
            String instance,
            String database
    ) {}
}