package com.example.nsu_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NsuBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(NsuBackendApplication.class, args);
    }

    //TODO: create method to fetch all queries under a post -> return in a 2d list
    //TODO: currently there is no way to guard against api users from setting the comment to be a child of a child comment
    //(no way to stop the nesting to be only 2 levels)

    //TODO: Add update user details/reset password logic
    //TODO: Set up DB Tables with flyway instead of jpa
    //TODO: Write unit tests
}
