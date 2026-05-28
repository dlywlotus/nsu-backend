package com.example.nsu_backend.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record CommentDetails(Long id,
                             String body,
                             UUID postId,
                             UUID authorId,
                             String username,
                             String profileIconImageKey,
                             Long parentCommentId,
                             OffsetDateTime createdAt,
                             List<CommentDetails> nestedComments) {
}
