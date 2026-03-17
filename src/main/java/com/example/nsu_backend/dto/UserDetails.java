package com.example.nsu_backend.dto;

import java.util.UUID;

public record UserDetails(UUID id, String username, String profileIconUrl) {
}
