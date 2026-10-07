package com.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pxp.kms.KmsClient;

@Configuration
public class KmsIntegrationConfig {

    @Value("${kms.server.url}")
    private String kmsUrl; // e.g., http://localhost:8080

    @Value("${kms.server.token}")
    private String apiToken;

    @Bean
    public KmsClient kmsClient() {
        // Initializes the SDK along with its internal high-performance Caffeine cache
        return new KmsClient(kmsUrl, apiToken);
    }
}
