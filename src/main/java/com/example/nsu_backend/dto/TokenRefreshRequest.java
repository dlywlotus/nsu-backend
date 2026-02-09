package com.example.nsu_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TokenRefreshRequest(@NotBlank String refreshTokenString,
                                  @NotNull UUID userId,
                                  @NotBlank String deviceId) {
}
