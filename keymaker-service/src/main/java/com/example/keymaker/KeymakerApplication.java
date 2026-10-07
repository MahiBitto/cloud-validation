package com.example.keymaker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.example.keymaker.config.KeymakerGcpProperties;

@SpringBootApplication
@EnableConfigurationProperties(KeymakerGcpProperties.class)
public class KeymakerApplication {

    public static void main(String[] args) {
        SpringApplication.run(KeymakerApplication.class, args);
    }
}
