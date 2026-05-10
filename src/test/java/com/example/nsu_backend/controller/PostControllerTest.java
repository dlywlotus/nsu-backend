package com.example.nsu_backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import com.example.nsu_backend.controllers.PostController;
import com.example.nsu_backend.dto.AddPostRequest;
import com.example.nsu_backend.services.AuthService;
import com.example.nsu_backend.services.CommentService;
import com.example.nsu_backend.services.PostService;

@WebMvcTest(PostController.class)
@AutoConfigureRestTestClient
@AutoConfigureMockMvc(addFilters = false)
public class PostControllerTest {
    @MockitoBean
    public CommentService commentService;
    @Autowired
    private RestTestClient client;
    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private PostService postService;

    @Test
    public void givenValidFields_whenCreatePost_shouldSucceed() {
        AddPostRequest request = new AddPostRequest("title", "body", "HOUSING");
        client.post().uri("/post").body(request).exchangeSuccessfully();
    }

//    @Test
//    public void givenEmptyBody_whenCreateComment_shouldFail() {
//        AddCommentRequest request = new AddCommentRequest("  ", UUID.randomUUID(), UUID.randomUUID(), 1L);
//        client.post().uri("/comment").body(request).exchange().expectStatus().is4xxClientError()
//                .expectBody().jsonPath("$.errors[*]")
//                .value((List<String> errors) -> assertThat(errors).contains(("body: must not be blank")));
//    }
//
//    @Test
//    public void givenTooLongBody_whenCreateComment_shouldFail() {
//        AddCommentRequest request = new AddCommentRequest("a".repeat((1001)), UUID.randomUUID(), UUID.randomUUID(), 1L);
//        client.post().uri("/comment").body(request).exchange().expectStatus().is4xxClientError()
//                .expectBody().jsonPath("$.errors[*]")
//                .value((List<String> errors) -> assertThat(errors).contains(("body: max character length is 1000 characters")));
//    }
//
//    @Test
//    public void givenNullPostId_whenCreateComment_shouldFail() {
//        AddCommentRequest request = new AddCommentRequest("a".repeat((1000)), null, UUID.randomUUID(), 1L);
//        client.post().uri("/comment").body(request).exchange().expectStatus().is4xxClientError()
//                .expectBody().jsonPath("$.errors[*]")
//                .value((List<String> errors) -> assertThat(errors).contains(("postId: must not be null")));
//    }

}
