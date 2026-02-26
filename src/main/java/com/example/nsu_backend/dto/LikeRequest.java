package com.example.nsu_backend.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record LikeRequest(@NotNull UUID userId, @NotNull UUID postId) {
}
