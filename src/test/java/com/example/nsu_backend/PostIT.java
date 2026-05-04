package com.example.nsu_backend;

import com.example.nsu_backend.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ActiveProfiles("local")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostIT {
    @LocalServerPort
    private int port;
    private WebTestClient client;
    @Autowired
    private JdbcClient jdbcClient;
    private String accessToken;

    @BeforeEach
    void beforeEach() {
        // Clear postgres tables
        List<String> tables = List.of("posts", "users");
        tables.forEach(table ->
                jdbcClient.sql("TRUNCATE TABLE " + table + " RESTART IDENTITY CASCADE").update()
        );

        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
        client.post().uri("/sign_up").bodyValue(new SignUpRequest("tester", "123123")).exchange();
        UserAuthResponse userAuthResponse = client.post().uri("/sign_in")
                .bodyValue(new SignInRequest("tester", "123123")).exchange().
                expectBody(UserAuthResponse.class).returnResult().getResponseBody();
        assertNotNull(userAuthResponse);
        accessToken = userAuthResponse.accessToken();
    }

    @Test
    public void createUpdateAndDeletePostSuccessfully() {
        // Create post
        PostDetails postDetails = client.post().uri("/post")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .bodyValue(new AddPostRequest("First post", "Post body", "EVENTS")).exchange()
                .expectBody(PostDetails.class).returnResult().getResponseBody();
        assertNotNull(postDetails);
        List<PostDetails> checkInsertRes = jdbcClient
                .sql("SELECT * FROM posts WHERE title = :title AND body = :body AND category = :category")
                .param("title", "First post").param("body", "Post body").param("category", "EVENTS")
                .query(PostDetails.class).list();
        // Verify that post was added
        assertFalse(checkInsertRes.isEmpty());

        // Update post
        UpdatePostRequest request = UpdatePostRequest.builder()
                .postId(postDetails.id()).category("HOUSING").build();
        client.put().uri("/post").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .bodyValue(request).exchangeSuccessfully();

        // Verify that post was updated
        PostDetails checkUpdateRes = client.get().uri("/post/" + postDetails.id())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).exchange()
                .expectBody(PostDetails.class).returnResult().getResponseBody();
        assertNotNull(checkUpdateRes);
        assertEquals("HOUSING", checkUpdateRes.category());

        // Delete post
        client.delete().uri("/post/" + postDetails.id())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).exchangeSuccessfully();
        // Verify that the post is deleted
        client.get().uri("/post/" + postDetails.id())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    public void givenInvalidCategory_whenCreatePost_throwException() {
        client.post().uri("/post")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .bodyValue(new AddPostRequest("First post", "Post body", "INVALID_EVENT"))
                .exchange().expectStatus().is4xxClientError();
    }

    @Test
    public void givenFilters_whenRetrievePosts_postsFiltered() {
        // Create posts
        PostDetails postOne = client.post().uri("/post")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .bodyValue(new AddPostRequest("First post", "Content by Alice", "EVENTS")).exchange()
                .expectBody(PostDetails.class).returnResult().getResponseBody();
        assertNotNull(postOne);
        PostDetails postTwo = client.post().uri("/post")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .bodyValue(new AddPostRequest("Second post", "Content by Bob", "HOUSING")).exchange()
                .expectBody(PostDetails.class).returnResult().getResponseBody();
        assertNotNull(postTwo);

        // Like post two
        client.post().uri("/like/" + postTwo.id()).header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).exchangeSuccessfully();

        List<PostDetails> singlePostPage = client.get().uri("/posts?page=0&size=1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).exchangeSuccessfully()
                .expectBodyList(PostDetails.class).returnResult().getResponseBody();
        assertNotNull(singlePostPage);
        assertEquals(1, singlePostPage.size());

        List<PostDetails> filterByCategory = client.get().uri("/posts?category=HOUSING")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).exchangeSuccessfully()
                .expectBodyList(PostDetails.class).returnResult().getResponseBody();
        assertNotNull(filterByCategory);
        assertEquals(filterByCategory.get(0).body(), postTwo.body());

        List<PostDetails> filterByTitle = client.get().uri("/posts?searchInput=First")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).exchangeSuccessfully()
                .expectBodyList(PostDetails.class).returnResult().getResponseBody();
        assertNotNull(filterByTitle);
        assertEquals(filterByTitle.get(0).body(), postOne.body());

        List<PostDetails> filterByBody = client.get().uri("/posts?searchInput=Alice")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).exchangeSuccessfully()
                .expectBodyList(PostDetails.class).returnResult().getResponseBody();
        assertNotNull(filterByBody);
        assertEquals(filterByBody.get(0).title(), postOne.title());

        List<PostDetails> filterBySearchAndCategory = client.get().uri("/posts?searchInput=First&category=HOUSING")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).exchangeSuccessfully()
                .expectBodyList(PostDetails.class).returnResult().getResponseBody();
        assertNotNull(filterBySearchAndCategory);
        assertTrue(filterBySearchAndCategory.isEmpty());

        List<PostDetails> sortByRecentAscending = client.get().uri("/posts?sort=created_at,asc")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).exchangeSuccessfully()
                .expectBodyList(PostDetails.class).returnResult().getResponseBody();
        assertNotNull(sortByRecentAscending);
        // The first post should be at the top of the list because it is older
        assertEquals(sortByRecentAscending.get(0).title(), postOne.title());

        List<PostDetails> sortByLikes = client.get().uri("/posts?sort=likes,desc")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).exchangeSuccessfully()
                .expectBodyList(PostDetails.class).returnResult().getResponseBody();
        assertNotNull(sortByLikes);
        assertEquals(sortByLikes.get(0).title(), postTwo.title());
    }
}

