package com.example.nsu_backend.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LogoutRequest(@NotNull UUID userId, @NotNull String deviceId) {
}
