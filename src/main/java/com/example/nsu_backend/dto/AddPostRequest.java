package com.example.nsu_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AddPostRequest {
    @NotBlank
    @Size(max = 250, message = "must be at at most 250 characters")
    private String title;

    @NotBlank
    @Size(max = 1000, message = "must be at at most 1000 characters")
    private String body;

    private String category;
}