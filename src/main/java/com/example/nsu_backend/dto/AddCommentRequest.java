package com.example.nsu_backend.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddCommentRequest(
        @NotBlank
        @Size(max = 1000, message = "max character length is 1000 characters")
        String body,

        @NotNull
        UUID postId,

        Long parentCommentId) {
}
