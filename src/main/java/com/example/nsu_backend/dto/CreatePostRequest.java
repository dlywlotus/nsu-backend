package com.example.nsu_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreatePostRequest(
        @NotBlank(message = "A post title is required.")
        @Size(max = 250, message = "Post title must be at at most 250 characters.")
        String title,

        @NotBlank(message = "A post body is required.")
        @Size(max = 1000, message = "Post title must be at at most 1000 characters.")
        String body,

        @NotBlank
        String category,

        @NotNull
        UUID authorId) {
}
