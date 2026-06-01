package com.example.nsu_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class NsuBackendApplication {

    // TODO: Implement rate limiting via bucket4j
    // TODO: Add a CDN (cloudflare)

    public static void main(String[] args) {
        SpringApplication.run(NsuBackendApplication.class, args);
    }
}
