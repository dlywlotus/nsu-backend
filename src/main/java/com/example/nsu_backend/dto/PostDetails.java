package com.example.nsu_backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PostDetails(UUID id, String title, String body,
                          String category, OffsetDateTime createdAt, UUID authorId) {
}
