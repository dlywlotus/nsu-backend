package com.example.nsu_backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record VerbosePostDetails(UUID id,
                                 String title,
                                 String body,
                                 String category,
                                 OffsetDateTime createdAt,
                                 UUID authorId,
                                 int likeCount,
                                 int commentCount,
                                 String username,
                                 String profileIconImageKey,
                                 boolean userLiked) {
}
