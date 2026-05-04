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
    @NotBlank(message = "A post title is required.")
    @Size(max = 250, message = "Post title must be at at most 250 characters.")
    private String title;

    @NotBlank(message = "A post body is required.")
    @Size(max = 1000, message = "Post title must be at at most 1000 characters.")
    private String body;

    private String category;
}