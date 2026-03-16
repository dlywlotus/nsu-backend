package com.example.nsu_backend.services;

import com.example.nsu_backend.dto.AddCommentRequest;
import com.example.nsu_backend.dto.CommentDetails;
import com.example.nsu_backend.entities.Comment;
import com.example.nsu_backend.mappers.CommentMapper;
import com.example.nsu_backend.repositories.CommentRepository;
import com.example.nsu_backend.repositories.PostRepository;
import com.example.nsu_backend.repositories.UserRepository;
import com.example.nsu_backend.utils.AuthUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentMapper commentMapper;
    private final AuthUtils authUtils;

    public CommentDetails addComment(AddCommentRequest request) {
        Comment comment = Comment.builder()
                .body(request.body())
                .author(userRepository.getReferenceById(request.authorId()))
                .post(postRepository.getReferenceById(request.postId()))
                .parentComment(request.parentCommentId() == null
                        ? null
                        : commentRepository.getReferenceById(request.parentCommentId()))
                .build();
        Comment newComment = commentRepository.save(comment);
        return commentMapper.commentToCommentDto(newComment);
    }

    public void deleteComment(Long id) {
        commentRepository.findByIdAndAuthorId(id, authUtils.getCurrentUserId())
                .orElseThrow(() -> new EntityNotFoundException("No comment found with the specified id"));
        commentRepository.deleteById(id);
    }
}
