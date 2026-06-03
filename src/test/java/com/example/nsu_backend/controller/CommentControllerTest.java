package com.example.nsu_backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import com.example.nsu_backend.controllers.CommentController;
import com.example.nsu_backend.dto.AddCommentRequest;
import com.example.nsu_backend.services.AccessTokenService;
import com.example.nsu_backend.services.CommentService;

@WebMvcTest(CommentController.class)
@AutoConfigureRestTestClient
@AutoConfigureMockMvc(addFilters = false)
public class CommentControllerTest {
    @Autowired
    private RestTestClient client;
    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private AccessTokenService accessTokenService;

    @Test
    public void givenValidCommentBody_whenCreateComment_shouldSucceed() {
        AddCommentRequest request = new AddCommentRequest("a".repeat((1000)), UUID.randomUUID(), 1L);
        client.post().uri("/comment").body(request).exchangeSuccessfully();
        verify(commentService, times(1)).addComment(any());
    }

    @Test
    public void givenEmptyBody_whenCreateComment_shouldFail() {
        AddCommentRequest request = new AddCommentRequest("  ", UUID.randomUUID(), 1L);
        client.post().uri("/comment").body(request).exchange().expectStatus().is4xxClientError()
                .expectBody().jsonPath("$.errors")
                .value((List<String> errors) -> assertThat(errors).contains(("body: must not be blank")));
        verify(commentService, never()).addComment(any());
    }

    @Test
    public void givenTooLongBody_whenCreateComment_shouldFail() {
        AddCommentRequest request = new AddCommentRequest("a".repeat((1001)), UUID.randomUUID(), 1L);
        client.post().uri("/comment").body(request).exchange().expectStatus().is4xxClientError()
                .expectBody().jsonPath("$.errors")
                .value((List<String> errors) -> assertThat(errors).contains(("body: max character length is 1000 characters")));
        verify(commentService, never()).addComment(any());
    }

    @Test
    public void givenNullPostId_whenCreateComment_shouldFail() {
        AddCommentRequest request = new AddCommentRequest("a".repeat((1000)), null, 1L);
        client.post().uri("/comment").body(request).exchange().expectStatus().is4xxClientError()
                .expectBody().jsonPath("$.errors")
                .value((List<String> errors) -> assertThat(errors).contains(("postId: must not be null")));
        verify(commentService, never()).addComment(any());
    }

}
