package com.example.nsu_backend.controllers;

import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.nsu_backend.dto.LikeDetails;
import com.example.nsu_backend.dto.LikeRequest;
import com.example.nsu_backend.services.LikeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("like")
public class LikeController {
    private final LikeService likeService;

    @PostMapping
    public LikeDetails addLike(@Valid @RequestBody LikeRequest request) {
        return likeService.addLike(request);
    }

    @DeleteMapping
    public Map<String, String> removeLike(@Valid @RequestBody LikeRequest request) {
        likeService.removeLike(request);
        return Map.of("message", "Like removed successfully.");
    }
}
