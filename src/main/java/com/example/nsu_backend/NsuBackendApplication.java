package com.example.nsu_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class NsuBackendApplication {

    // TODO: Implement some sort of rate limiting

    public static void main(String[] args) {
        SpringApplication.run(NsuBackendApplication.class, args);
    }
}
