package com.example.nsu_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NsuBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(NsuBackendApplication.class, args);
    }

    // TODO: Add unit and integration tests for auth
    // TODO: Add tests for multi device log
    // TODO: Add MinIO and image store
}
