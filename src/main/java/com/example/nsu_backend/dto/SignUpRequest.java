package com.example.nsu_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @NotBlank
        @Size(min = 3, message = "Must be at least 3 characters.")
        @Size(max = 15, message = "Must be at at most 15 characters.")
        String username,

        @NotBlank
        @Size(min = 6, max = 15, message = "Must be between 6 and 15 characters.")
        String password) {
}
