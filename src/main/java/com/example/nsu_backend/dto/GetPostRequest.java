package com.example.nsu_backend.dto;

import org.springframework.data.domain.Pageable;

import com.example.nsu_backend.enums.Category;

public record GetPostRequest(Category category, String searchInput, String authorId,
                             Pageable pageable) {
}
