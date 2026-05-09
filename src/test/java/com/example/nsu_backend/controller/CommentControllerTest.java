package com.example.nsu_backend.controller;

import com.example.nsu_backend.controllers.CommentController;
import com.example.nsu_backend.dto.AddCommentRequest;
import com.example.nsu_backend.services.AuthService;
import com.example.nsu_backend.services.CommentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.UUID;

@WebMvcTest(CommentController.class)
@AutoConfigureRestTestClient
@AutoConfigureMockMvc(addFilters = false)
public class CommentControllerTest {
    @Autowired
    private RestTestClient client;
    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private CommentService commentService;

    @Test
    public void givenEmptyBody_whenCreateComment_shouldFail() {
        AddCommentRequest request = new AddCommentRequest("  ", UUID.randomUUID(), UUID.randomUUID(), 1L);
        client.post().uri("/comment").contentType(MediaType.APPLICATION_JSON).body(request).exchangeSuccessfully();
    }

}
