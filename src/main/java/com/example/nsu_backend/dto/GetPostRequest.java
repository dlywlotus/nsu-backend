package com.example.nsu_backend.dto;

import org.springframework.data.domain.Pageable;

import java.util.UUID;

public record GetPostRequest(String category, String searchInput, UUID authorId, Pageable pageable) {
}
