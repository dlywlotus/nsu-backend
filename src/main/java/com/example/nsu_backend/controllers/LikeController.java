package com.example.nsu_backend.controllers;

import java.util.Map;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.nsu_backend.services.LikeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("like")
public class LikeController {
    private final LikeService likeService;

    @PostMapping
    public Map<String, String> addLike(@RequestParam UUID postId) {
        likeService.addLike(postId);
        return Map.of("message", "Like added successfully.");
    }

    @DeleteMapping
    public Map<String, String> removeLike(@RequestParam UUID postId) {
        likeService.removeLike(postId);
        return Map.of("message", "Like removed successfully.");
    }
}
