package com.example.nsu_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddCommentRequest(
        @NotBlank
        String body,

        @NotNull
        UUID postId,

        @NotNull
        UUID authorId,

        Long parentCommentId) {
}
