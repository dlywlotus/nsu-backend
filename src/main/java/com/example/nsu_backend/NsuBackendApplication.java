package com.example.nsu_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


// TODO: Set up integration tests for comment and likes
// TODO: Test if adding/ deleting comments/likes updates post comment/like count

// TODO: Migrate all integration tests to use test containers
// BUG: when delete parent comment child comments are not deleted


@EnableScheduling
@SpringBootApplication
public class NsuBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(NsuBackendApplication.class, args);
    }
}
