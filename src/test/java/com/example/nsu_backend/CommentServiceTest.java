package com.example.nsu_backend;

import com.example.nsu_backend.dto.AddCommentRequest;
import com.example.nsu_backend.dto.CommentDetails;
import com.example.nsu_backend.entities.Comment;
import com.example.nsu_backend.entities.Post;
import com.example.nsu_backend.entities.User;
import com.example.nsu_backend.exceptions.NestedCommentException;
import com.example.nsu_backend.mappers.CommentMapper;
import com.example.nsu_backend.repositories.CommentRepository;
import com.example.nsu_backend.repositories.PostRepository;
import com.example.nsu_backend.repositories.UserRepository;
import com.example.nsu_backend.services.CommentService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PostRepository postRepository;

    @Spy
    private CommentMapper commentMapper = Mappers.getMapper(CommentMapper.class);

    @InjectMocks
    private CommentService commentService;

    @Test
    public void givenParentNotFound_whenCreateComment_throwException() {
        // Treat comment 999 as a comment that does not exist
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () ->
                commentService.addComment(new AddCommentRequest("New comment", UUID.randomUUID(), UUID.randomUUID(), 999L)));
    }

    @Test
    public void givenParentIsAlreadyNested_whenCreateComment_throwException() {
        UUID userOneId = UUID.randomUUID();
        UUID postOneId = UUID.randomUUID();
        User userOne = User.builder().id(userOneId).build();
        Post postOne = Post.builder().id(postOneId).build();
        Comment topLevelComment = Comment.builder().id(1L).post(postOne).author(userOne).build();
        Comment nestedComment = Comment.builder().id(3L).post(postOne).author(userOne).parentComment(topLevelComment).build();

        when(commentRepository.findById(2L)).thenReturn(Optional.of(nestedComment));

        assertThrows(NestedCommentException.class, () ->
                commentService.addComment(new AddCommentRequest("New comment", postOneId, userOneId, 2L)));
    }


    @Test
    public void givenValidParentComment_whenAddComment_shouldAddSuccessfully() {
        UUID userOneId = UUID.randomUUID();
        UUID postOneId = UUID.randomUUID();
        User userOne = User.builder().id(userOneId).build();
        Post postOne = Post.builder().id(postOneId).build();
        Comment topLevelComment = Comment.builder().id(1L).post(postOne).author(userOne).build();

        AddCommentRequest newCommentRequest = new AddCommentRequest("New comment", postOneId, userOneId, 1L);
        Comment newComment = Comment.builder().id(2L).body("New comment").post(postOne).author(userOne).parentComment(topLevelComment).build();
        CommentDetails commentDto = new CommentDetails(2L, "New comment", postOneId, userOneId, 1L, OffsetDateTime.now(), List.of());

        when(commentRepository.findById(1L)).thenReturn(Optional.of(topLevelComment));
        when(commentRepository.save(any())).thenReturn(newComment);

        CommentDetails newCommentDto = commentService.addComment(newCommentRequest);
        assertAll(
                () -> assertEquals(commentDto.body(), newCommentDto.body()),
                () -> assertEquals(commentDto.parentCommentId(), newCommentDto.parentCommentId()),
                () -> assertEquals(commentDto.postId(), newCommentDto.postId()),
                () -> assertEquals(commentDto.authorId(), newCommentDto.authorId())
        );
    }

    @Test
    public void givenFlatCommentsList_whenFetchComments_shouldGroupByTopLevel() {
        UUID userOneId = UUID.randomUUID();
        UUID postOneId = UUID.randomUUID();
        User userOne = User.builder().id(userOneId).build();
        Post postOne = Post.builder().id(postOneId).build();

        Comment topLevelCommentOne = Comment.builder().id(1L).post(postOne).author(userOne).build();
        Comment topLevelCommentTwo = Comment.builder().id(2L).post(postOne).author(userOne).build();
        Comment nestedCommentOne = Comment.builder().id(3L).post(postOne).author(userOne).parentComment(topLevelCommentOne).build();
        Comment nestedCommentTwo = Comment.builder().id(4L).post(postOne).author(userOne).parentComment(topLevelCommentOne).build();

        List<Comment> commentList = List.of(topLevelCommentOne, topLevelCommentTwo, nestedCommentOne, nestedCommentTwo);
        when(commentRepository.findByPostId(postOneId)).thenReturn(commentList);

        List<CommentDetails> commentDetailsList = commentService.getCommentsByPost(postOneId);
        // Verify that two of the four comments are top level ones
        assertEquals(2, commentDetailsList.size());

        CommentDetails topLevelCommentOneDetails = commentDetailsList.stream()
                .filter(commentDetails -> commentDetails.id().equals(1L)).findFirst().orElseThrow();
        CommentDetails topLevelCommentTwoDetails = commentDetailsList.stream()
                .filter(commentDetails -> commentDetails.id().equals(2L)).findFirst().orElseThrow();

        // Verify that top level comment one contains both nested comment one and two
        assertEquals(2, topLevelCommentOneDetails.nestedComments().stream()
                .filter(commentDetails ->
                        commentDetails.id().equals(nestedCommentOne.getId()) ||
                                commentDetails.id().equals(nestedCommentTwo.getId())).count());
        // Verify that top level comment two has no children
        assertTrue(topLevelCommentTwoDetails.nestedComments().isEmpty());
    }
}