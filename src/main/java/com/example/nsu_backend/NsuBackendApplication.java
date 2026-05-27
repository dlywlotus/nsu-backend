package com.example.nsu_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


// TODO: Set up integration tests for likes

// TODO: Migrate all integration tests to use test containers


@EnableScheduling
@SpringBootApplication
public class NsuBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(NsuBackendApplication.class, args);
    }
}
