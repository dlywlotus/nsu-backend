package com.example.nsu_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NsuBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(NsuBackendApplication.class, args);
    }

    //TODO: Implement comments logic

    //1. comments have one level nesting
    //2. fields: body, postId, authorId, parentCommentId, createdAt
    //3. comments are only fetched when page is expanded

    //TODO: Add update user details/reset password logic
    //TODO: Set up DB Tables with flyway instead of jpa
    //TODO: Write unit tests
}
