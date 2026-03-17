package com.example.nsu_backend.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UpdatePostRequest {
    @Size(max = 250, message = "Post title must be at at most 250 characters.")
    private String title;

    @Size(max = 1000, message = "Post title must be at at most 1000 characters.")
    private String body;

    private String category;

    private int likeCount;

    @NotNull
    private UUID postId;
}
