package com.example.nsu_backend.dto;

import java.util.UUID;

public record LikeDetails(Long id, UUID userId, UUID postId) {
}
