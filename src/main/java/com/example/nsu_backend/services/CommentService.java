package com.example.nsu_backend.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.nsu_backend.dto.AddCommentRequest;
import com.example.nsu_backend.dto.CommentDetails;
import com.example.nsu_backend.entities.Comment;
import com.example.nsu_backend.exceptions.NestedCommentException;
import com.example.nsu_backend.mappers.CommentMapper;
import com.example.nsu_backend.repositories.CommentRepository;
import com.example.nsu_backend.repositories.PostRepository;
import com.example.nsu_backend.repositories.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentMapper commentMapper;
    private final UserService userService;
    private final PostService postService;

    @Transactional
    public CommentDetails addComment(AddCommentRequest request) {
        if (request.parentCommentId() != null) {
            Comment parentComment = commentRepository.findById(request.parentCommentId()).orElseThrow(
                    () -> new EntityNotFoundException("Parent comment not found."));
            //Check if the parent comment is a top level comment
            if (parentComment.getParentComment() != null) {
                throw new NestedCommentException("Comments can only be nested one level deep.");
            }
        }

        Comment comment = Comment.builder()
                .body(request.body())
                .author(userRepository.getReferenceById(userService.getCurrentUserId()))
                .post(postRepository.getReferenceById(request.postId()))
                .parentComment(request.parentCommentId() == null
                        ? null
                        : commentRepository.getReferenceById(request.parentCommentId()))
                .build();
        Comment newComment = commentRepository.save(comment);
        postService.updateCommentCount(request.postId(), true);
        return commentMapper.commentToCommentDto(newComment);
    }

    @Transactional
    public void deleteComment(Long id) {
        Comment commentToDelete = commentRepository.findByIdAndAuthorId(id, userService.getCurrentUserId())
                .orElseThrow(() -> new EntityNotFoundException("Comment not found."));
        commentRepository.deleteById(id);
        postService.updateCommentCount(commentToDelete.getPost().getId(), false);
    }

    public List<CommentDetails> getCommentsByPost(UUID postId) {
        HashMap<Long, List<CommentDetails>> idToNestedCommentsMap = new HashMap<>();
        HashMap<Long, CommentDetails> idToDetailsMap = new HashMap<>();
        List<CommentDetails> nestedComments = new ArrayList<>();
        commentRepository.findByPostId(postId).stream().map(commentMapper::commentToCommentDto).forEach(c -> {
            if (c.parentCommentId() == null) {
                idToNestedCommentsMap.put(c.id(), new ArrayList<>());
            } else {
                nestedComments.add(c);
            }
            idToDetailsMap.put(c.id(), c);
        });

        nestedComments.forEach(c -> idToNestedCommentsMap.get(c.parentCommentId()).add(c));
        List<CommentDetails> res = new ArrayList<>();
        for (Map.Entry<Long, List<CommentDetails>> entry : idToNestedCommentsMap.entrySet()) {
            List<CommentDetails> childComments = entry.getValue();
            CommentDetails parentComment = idToDetailsMap.get(entry.getKey());
            CommentDetails newParentComment = commentMapper.commentDtoToParentCommentDto(parentComment, childComments);
            res.add(newParentComment);
        }

        return res;
    }
}
