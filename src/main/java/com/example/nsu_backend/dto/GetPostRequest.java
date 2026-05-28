package com.example.nsu_backend.dto;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.example.nsu_backend.enums.Category;

public record GetPostRequest(Category category, String searchInput, UUID authorId,
                             Pageable pageable) {
}
