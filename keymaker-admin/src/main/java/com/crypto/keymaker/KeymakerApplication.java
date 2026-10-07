package com.crypto.keymaker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.google.cloud.spring.autoconfigure.storage.GcpStorageAutoConfiguration;
import com.google.cloud.spring.data.spanner.repository.config.EnableSpannerRepositories;

@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class,
    GcpStorageAutoConfiguration.class
})
@EnableScheduling
@EnableSpannerRepositories(basePackages = "com.crypto.keymaker.repository")
public class KeymakerApplication {
    public static void main(String[] args) {
        SpringApplication.run(KeymakerApplication.class, args);
    }
}
