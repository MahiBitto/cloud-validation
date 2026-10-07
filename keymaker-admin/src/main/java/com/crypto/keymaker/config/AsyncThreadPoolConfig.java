package com.crypto.keymaker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncThreadPoolConfig {
    @Bean(name = "gcsExecutorPool")
    public Executor gcsExecutorPool() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(15);
        exec.setMaxPoolSize(40);
        exec.setQueueCapacity(1000);
        exec.setThreadNamePrefix("KMS-GCS-Sync-");
        exec.initialize();
        return exec;
    }
}
