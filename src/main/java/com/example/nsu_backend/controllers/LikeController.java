package com.example.nsu_backend.controllers;

import com.example.nsu_backend.services.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("like")
public class LikeController {
    private final LikeService likeService;

    @PostMapping("{postId}")
    public Map<String, String> addLike(@PathVariable UUID postId) {
        likeService.addLike(postId);
        return Map.of("message", "Like added successfully.");
    }

    @DeleteMapping("{postId}")
    public Map<String, String> removeLike(@PathVariable UUID postId) {
        likeService.removeLike(postId);
        return Map.of("message", "Like removed successfully.");
    }
}
