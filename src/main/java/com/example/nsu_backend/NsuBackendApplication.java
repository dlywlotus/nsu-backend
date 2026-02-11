package com.example.nsu_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NsuBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(NsuBackendApplication.class, args);
    }

    //TODO: Add update post logic
    //TODO: Add update user details/reset password logic
    //TODO: Set up DB Tables with flyway instead of jpa
    //TODO: Implement comments logic
    //TODO: Write unit tests
}
