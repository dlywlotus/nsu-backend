package com.example.nsu_backend.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DeletePostRequest(@NotNull UUID postId) {
}
