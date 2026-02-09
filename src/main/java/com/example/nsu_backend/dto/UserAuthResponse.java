package com.example.nsu_backend.dto;

import java.util.UUID;

public record UserAuthResponse(
        AuthTokensResponse authTokens,
        UUID userId) {
}