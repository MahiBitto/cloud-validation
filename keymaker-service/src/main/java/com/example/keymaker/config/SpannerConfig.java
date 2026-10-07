package com.example.keymaker.config;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpannerConfig {

    private final KeymakerGcpProperties properties;

    public SpannerConfig(KeymakerGcpProperties properties) {
        this.properties = properties;
    }

    @Bean
    public Spanner spanner() {
        return SpannerOptions.newBuilder()
                .setProjectId(properties.projectId())
                .build()
                .getService();
    }

    @Bean
    public DatabaseClient databaseClient(Spanner spanner) {

        DatabaseId databaseId =
                DatabaseId.of(
                        properties.projectId(),
                        properties.spanner().instance(),
                        properties.spanner().database()
                );

        return spanner.getDatabaseClient(databaseId);
    }
}