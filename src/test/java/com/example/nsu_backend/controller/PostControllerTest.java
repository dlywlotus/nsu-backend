package com.example.nsu_backend.controller;

import com.example.nsu_backend.controllers.PostController;
import com.example.nsu_backend.dto.AddPostRequest;
import com.example.nsu_backend.dto.UpdatePostRequest;
import com.example.nsu_backend.enums.Category;
import com.example.nsu_backend.services.AccessTokenService;
import com.example.nsu_backend.services.CommentService;
import com.example.nsu_backend.services.PostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@WebMvcTest(PostController.class)
@AutoConfigureRestTestClient
@AutoConfigureMockMvc(addFilters = false)
public class PostControllerTest {
    @MockitoBean
    public CommentService commentService;
    @Autowired
    private RestTestClient client;
    @MockitoBean
    private AccessTokenService accessTokenService;
    @MockitoBean
    private PostService postService;

    @Test
    public void givenValidFields_whenRetrievingPosts_shouldSucceed() {
        client.get().uri("/posts?category=HOUSING&page=0&size=20&sort=createdAt,desc").exchangeSuccessfully();
        verify(postService, times(1))
                .getPosts(argThat(req -> req.category() == Category.HOUSING &&
                        req.pageable().getPageNumber() == 0 &&
                        req.pageable().getPageSize() == 20 &&
                        req.pageable().getSort().stream().anyMatch((Sort.Order order) ->
                                order.getProperty().equals("createdAt") && order.getDirection().isDescending()
                        )
                ));
    }

    @Test
    public void givenNoQueryParameters_whenRetrievingPosts_shouldStillSucceed() {
        client.get().uri("/posts").exchangeSuccessfully();
        verify(postService, times(1)).getPosts(any());
    }

    @Test
    public void givenInvalidCategory_whenRetrievingPosts_shouldFail() {
        client.get().uri("/posts?category=INVALID&page=0&size=20&sort=createdAt,desc").exchange().expectStatus().is4xxClientError();
        verify(postService, never()).getPosts(any());
    }

    @Test
    public void givenValidFields_whenCreatePost_shouldSucceed() {
        AddPostRequest request = new AddPostRequest("a".repeat(250), "a".repeat(1000), Category.EVENTS);
        client.post().uri("/post").body(request).exchangeSuccessfully();
        verify(postService, times(1)).createPost(any());
    }

    @Test
    public void givenTooLongTitle_whenCreatePost_shouldFail() {
        AddPostRequest request = new AddPostRequest("a".repeat(251), "a".repeat(1000), Category.EVENTS);
        client.post().uri("/post").body(request).exchange().expectStatus().is4xxClientError()
                .expectBody()
                .jsonPath("$.errors")
                .value((List<String> errors) -> assertThat(errors).contains("title: must be at at most 250 characters"));
        verify(postService, never()).createPost(any());
    }

    @Test
    public void givenTooLongBody_whenCreatePost_shouldFail() {
        AddPostRequest request = new AddPostRequest("a".repeat(250), "a".repeat(1001), Category.EVENTS);
        client.post().uri("/post").body(request).exchange().expectStatus().is4xxClientError()
                .expectBody()
                .jsonPath("$.errors")
                .value((List<String> errors) -> assertThat(errors).contains("body: must be at at most 1000 characters"));
        verify(postService, never()).createPost(any());
    }

    @Test
    public void givenValidFields_whenUpdatePost_shouldSucceed() {
        UpdatePostRequest request = new UpdatePostRequest("a".repeat(250), "a".repeat(1000), Category.EVENTS, UUID.randomUUID());
        client.put().uri("/post").body(request).exchangeSuccessfully();
        verify(postService, times(1)).updatePost(any());
    }

    @Test
    public void givenTooLongTitle_whenUpdatePost_shouldFail() {
        UpdatePostRequest request = new UpdatePostRequest("a".repeat(251), "a".repeat(1000), Category.EVENTS, UUID.randomUUID());
        client.put().uri("/post").body(request).exchange().expectStatus().is4xxClientError()
                .expectBody()
                .jsonPath("$.errors")
                .value((List<String> errors) -> assertThat(errors).contains("title: must be at at most 250 characters"));
        verify(postService, never()).updatePost(any());
    }

    @Test
    public void givenTooLongBody_whenUpdatePost_shouldFail() {
        UpdatePostRequest request = new UpdatePostRequest("a".repeat(250), "a".repeat(1001), Category.EVENTS, UUID.randomUUID());
        client.put().uri("/post").body(request).exchange().expectStatus().is4xxClientError()
                .expectBody()
                .jsonPath("$.errors")
                .value((List<String> errors) -> assertThat(errors).contains("body: must be at at most 1000 characters"));
        verify(postService, never()).updatePost(any());
    }

}
