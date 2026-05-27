package com.example.nsu_backend.services;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.nsu_backend.entities.Like;
import com.example.nsu_backend.repositories.LikeRepository;
import com.example.nsu_backend.repositories.PostRepository;
import com.example.nsu_backend.repositories.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final UserService userService;
    private final PostService postService;

    @Transactional
    public void addLike(UUID postId) {
        UUID userId = userService.getCurrentUserId();
        Like like = Like.builder()
                .user(userRepository.getReferenceById(userId))
                .post(postRepository.getReferenceById(postId))
                .build();
        likeRepository.save(like);
        postService.updateLikeCount(postId, 1);
    }

    @Transactional
    public void removeLike(UUID postId) {
        UUID userId = userService.getCurrentUserId();
        likeRepository.deleteByUserIdAndPostId(userId, postId);
        postService.updateLikeCount(postId, -1);
    }
}
