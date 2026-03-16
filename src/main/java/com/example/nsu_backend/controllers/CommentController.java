package com.example.nsu_backend.controllers;

import com.example.nsu_backend.dto.AddCommentRequest;
import com.example.nsu_backend.dto.CommentDetails;
import com.example.nsu_backend.services.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("comment")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public CommentDetails createComment(@RequestBody @Valid AddCommentRequest request) {
        return commentService.addComment(request);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return Map.of("message", "The comment has been deleted successfully!");
    }
}
