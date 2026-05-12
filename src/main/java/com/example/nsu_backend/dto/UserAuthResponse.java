package com.example.nsu_backend.dto;

import java.util.UUID;

public record UserAuthResponse(String accessToken, UUID userId) {
}