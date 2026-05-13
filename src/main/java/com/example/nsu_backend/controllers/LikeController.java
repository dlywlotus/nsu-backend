package com.example.nsu_backend.controllers;

import com.example.nsu_backend.dto.MessageResponse;
import com.example.nsu_backend.services.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("like")
public class LikeController {
    private final LikeService likeService;

    @PostMapping("{postId}")
    public MessageResponse addLike(@PathVariable UUID postId) {
        likeService.addLike(postId);
        return new MessageResponse("Like added successfully.");
    }

    @DeleteMapping("{postId}")
    public MessageResponse removeLike(@PathVariable UUID postId) {
        likeService.removeLike(postId);
        return new MessageResponse("Like removed successfully.");
    }
}
