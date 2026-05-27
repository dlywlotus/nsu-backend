package com.example.nsu_backend.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.nsu_backend.dto.AddCommentRequest;
import com.example.nsu_backend.dto.AddPostRequest;
import com.example.nsu_backend.dto.CommentDetails;
import com.example.nsu_backend.dto.SignUpRequest;
import com.example.nsu_backend.dto.UserDetails;
import com.example.nsu_backend.enums.Category;
import com.example.nsu_backend.services.CommentService;
import com.example.nsu_backend.services.PostService;
import com.example.nsu_backend.services.UserService;
import com.example.nsu_backend.utils.PostgresUtils;

@SpringBootTest
@Testcontainers
@ActiveProfiles("local")
public class CommentIT {
    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");
    private static UUID postId;
    @Autowired
    private CommentService commentService;
    @Autowired
    private PostService postService;
    @Autowired
    private UserService userService;
    @Autowired
    private PostgresUtils postgresUtils;

    @BeforeEach
    void beforeEach() {
        // Set up user and post
        postgresUtils.clear();
        UserDetails newUser = userService.saveUser(new SignUpRequest("tester", "123123"));
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(newUser.id(), null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        postId = postService.createPost(new AddPostRequest("Title", "Body", Category.EVENTS)).id();
    }

    @Test
    void whenAddAndRemoveComment_commentCountOfPostShouldIncreaseAndDecrease() {
        commentService.addComment(new AddCommentRequest("commentOne", postId, null));
        CommentDetails commentTwo = commentService.addComment(new AddCommentRequest("commentTwo", postId, null));
        assertThat(postService.getPost(postId).commentCount()).isEqualTo(2);
        commentService.deleteComment(commentTwo.id());
        assertThat(postService.getPost(postId).commentCount()).isEqualTo(1);
    }

    @Test
    void givenCommentIsNested_whenAddComment_commentCountShouldStillIncrease() {
        CommentDetails parentComment = commentService.addComment(new AddCommentRequest("parentComment", postId, null));
        commentService.addComment(new AddCommentRequest("nestedComment", postId, parentComment.id()));
        assertThat(postService.getPost(postId).commentCount()).isEqualTo(2);
    }

    @Test
    void givenNestedComments_whenDeleteParentComment_commentCountShouldDecreaseCorrectly() {
        CommentDetails parentComment = commentService.addComment(new AddCommentRequest("parentComment", postId, null));
        commentService.addComment(new AddCommentRequest("nestedCommentOne", postId, parentComment.id()));
        commentService.addComment(new AddCommentRequest("nestedCommentTwo", postId, parentComment.id()));
        commentService.addComment(new AddCommentRequest("nestedCommentThree", postId, parentComment.id()));
        assertThat(postService.getPost(postId).commentCount()).isEqualTo(4);
        commentService.deleteComment(parentComment.id());
        assertThat(postService.getPost(postId).commentCount()).isEqualTo(0);
    }
}
