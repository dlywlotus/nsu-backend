package com.example.nsu_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// TODO: Set up integration tests for comment and likes
// TODO: Tests should spin up test containers and inject authenticated user manually or with @WithMockUser
// TODO: Test if adding/ deleting comments/likes updates post comment/like count

// TODO: Update single post GET route to return VerbosePostDetails


@EnableScheduling
@SpringBootApplication
public class NsuBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(NsuBackendApplication.class, args);
    }
}
