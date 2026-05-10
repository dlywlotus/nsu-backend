package com.example.nsu_backend.dto;

import java.util.UUID;

import com.example.nsu_backend.enums.Category;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePostRequest {
    @Size(max = 250, message = "must be at at most 250 characters")
    private String title;

    @Size(max = 1000, message = "must be at at most 1000 characters")
    private String body;

    private Category category;

    @NotNull
    private UUID postId;
}
