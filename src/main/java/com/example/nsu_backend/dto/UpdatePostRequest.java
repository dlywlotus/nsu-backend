package com.example.nsu_backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePostRequest {
    @Size(max = 250, message = "Post title must be at at most 250 characters.")
    private String title;

    @Size(max = 1000, message = "Post title must be at at most 1000 characters.")
    private String body;

    private String category;

    @NotNull
    private UUID postId;
}
