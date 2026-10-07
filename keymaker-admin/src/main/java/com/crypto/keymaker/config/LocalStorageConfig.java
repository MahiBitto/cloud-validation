package com.crypto.keymaker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@Configuration
public class LocalStorageConfig {

    @Bean
    public Storage googleCloudStorageClient() {
        // Initializes a safe instance using local developer/emulator defaults
        return StorageOptions.getDefaultInstance().getService();
    }
}
