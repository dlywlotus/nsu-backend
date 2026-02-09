package com.example.nsu_backend.controllers;

import com.example.nsu_backend.dto.CreatePostRequest;
import com.example.nsu_backend.dto.DeletePostRequest;
import com.example.nsu_backend.dto.PostDetails;
import com.example.nsu_backend.services.PostService;
import com.example.nsu_backend.utils.AuthUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("post")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final AuthUtils authUtils;

    @GetMapping
    public List<PostDetails> getPosts() {
        return postService.getPosts();
    }

    @PostMapping
    public PostDetails createPost(@Valid @RequestBody CreatePostRequest request) {
        String userId = authUtils.getCurrentUserId();
        if (!userId.equals(request.authorId().toString())) {
            throw new AuthorizationDeniedException("Can't create a post on behalf of another user.");
        }
        return postService.createPost(request);
    }

    @DeleteMapping
    public Map<String, String> deletePost(@Valid @RequestBody DeletePostRequest request) {
        postService.deletePost(request);
        return Map.of("message", "Post " + request.postId() + " has been deleted.");
    }
}
