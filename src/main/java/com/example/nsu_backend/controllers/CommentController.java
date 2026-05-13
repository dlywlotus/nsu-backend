package com.example.nsu_backend.controllers;

import com.example.nsu_backend.dto.AddCommentRequest;
import com.example.nsu_backend.dto.CommentDetails;
import com.example.nsu_backend.dto.MessageResponse;
import com.example.nsu_backend.services.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
