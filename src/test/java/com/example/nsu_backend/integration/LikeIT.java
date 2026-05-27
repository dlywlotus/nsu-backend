package com.example.nsu_backend.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.nsu_backend.dto.AddPostRequest;
import com.example.nsu_backend.dto.SignUpRequest;
import com.example.nsu_backend.dto.UserDetails;
import com.example.nsu_backend.enums.Category;
import com.example.nsu_backend.services.LikeService;
import com.example.nsu_backend.services.PostService;
import com.example.nsu_backend.services.UserService;
import com.example.nsu_backend.utils.PostgresUtils;

@SpringBootTest
@Testcontainers
@ActiveProfiles("local")
public class LikeIT {
    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");
    private static UUID postId;
    @Autowired
    private LikeService likeService;
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
    void whenAddAndRemoveLike_likeCountOfPostShouldIncreaseAndDecrease() {
        likeService.addLike(postId);
        assertThat(postService.getPost(postId).likeCount()).isEqualTo(1);
        likeService.removeLike(postId);
        assertThat(postService.getPost(postId).likeCount()).isEqualTo(0);
    }

    @Test
    void givenAlreadyLiked_whenLikePost_shouldThrowError() {
        likeService.addLike(postId);
        assertThat(postService.getPost(postId).likeCount()).isEqualTo(1);
        assertThrows(DataIntegrityViolationException.class, () -> likeService.addLike(postId));
    }
}
