package com.example.nsu_backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddCommentRequest(
        @NotBlank
        @Max(value = 1000, message = "Comments have a max character length of 1000 characters!")
        String body,

        @NotNull
        UUID postId,

        @NotNull
        UUID authorId,

        Long parentCommentId) {
}
