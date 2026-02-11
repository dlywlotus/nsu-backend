package com.example.nsu_backend.controllers;

import com.example.nsu_backend.dto.LikeDetails;
import com.example.nsu_backend.dto.LikeRequest;
import com.example.nsu_backend.services.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("like")
public class LikeController {
    private final LikeService likeService;

    @PostMapping
    public LikeDetails addLike(@RequestBody LikeRequest request) {
        return likeService.addLike(request);
    }

    @DeleteMapping
    public Map<String, String> removeLike(@RequestBody LikeRequest request) {
        likeService.removeLike(request);
        return Map.of("message", "Like removed successfully.");
    }
}
