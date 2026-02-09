package com.example.nsu_backend.dto;

public record AuthTokensResponse(
        String accessToken,
        String refreshToken,
        String accessTokenExpiration) {
}