package com.example.nsu_backend.dto;

import java.util.UUID;

public record LikeRequest(UUID userId, UUID postId) {
}
