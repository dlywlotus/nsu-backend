package com.example.nsu_backend.controllers;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.nsu_backend.dto.AddCommentRequest;
import com.example.nsu_backend.dto.CommentDetails;
import com.example.nsu_backend.dto.MessageResponse;
import com.example.nsu_backend.services.CommentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
    public MessageResponse deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return new MessageResponse("The comment has been deleted successfully!");
    }
}
