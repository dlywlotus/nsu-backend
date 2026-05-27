package com.example.nsu_backend.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.nsu_backend.dto.AddPostRequest;
import com.example.nsu_backend.dto.PageOfPosts;
import com.example.nsu_backend.dto.PostDetails;
import com.example.nsu_backend.dto.SignInRequest;
import com.example.nsu_backend.dto.SignUpRequest;
import com.example.nsu_backend.dto.UpdatePostRequest;
import com.example.nsu_backend.dto.UserAuthResponse;
import com.example.nsu_backend.enums.Category;
import com.example.nsu_backend.utils.PostgresUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("local")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostIT {
    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @LocalServerPort
    private int port;
    @Autowired
    private JdbcClient jdbcClient;
    @Autowired
    private PostgresUtils postgresUtils;

    private WebTestClient client;
    private String accessToken;

    @BeforeEach
    void beforeEach() {
        postgresUtils.clear();
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
                .bodyValue(new AddPostRequest("First post", "Post body", Category.EVENTS)).exchangeSuccessfully()
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
                .postId(postDetails.id()).category(Category.HOUSING).build();
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
    public void givenFilters_whenRetrievePosts_postsFiltered() {
        // Create posts
        PostDetails postOne = client.post().uri("/post")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .bodyValue(new AddPostRequest("First post", "Content by Alice", Category.EVENTS)).exchange()
                .expectBody(PostDetails.class).returnResult().getResponseBody();
        assertNotNull(postOne);
        PostDetails postTwo = client.post().uri("/post")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .bodyValue(new AddPostRequest("Second post", "Content by Bob", Category.HOUSING)).exchange()
                .expectBody(PostDetails.class).returnResult().getResponseBody();
        assertNotNull(postTwo);

        // Like post two
        client.post().uri("/like/" + postTwo.id()).header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).exchangeSuccessfully();

        PageOfPosts singlePostPage = client.get().uri("/posts?page=0&size=1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).exchangeSuccessfully()
                .expectBody(PageOfPosts.class).returnResult().getResponseBody();
        assertNotNull(singlePostPage);
        assertEquals(1, singlePostPage.posts().size());

        PageOfPosts filterByCategory = client.get().uri("/posts?category=HOUSING")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).exchangeSuccessfully()
                .expectBody(PageOfPosts.class).returnResult().getResponseBody();
        assertNotNull(filterByCategory);
        assertEquals(filterByCategory.posts().get(0).body(), postTwo.body());

        PageOfPosts filterByTitle = client.get().uri("/posts?searchInput=First")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).exchangeSuccessfully()
                .expectBody(PageOfPosts.class).returnResult().getResponseBody();
        assertNotNull(filterByTitle);
        assertEquals(filterByTitle.posts().get(0).body(), postOne.body());

        PageOfPosts filterByBody = client.get().uri("/posts?searchInput=Alice")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).exchangeSuccessfully()
                .expectBody(PageOfPosts.class).returnResult().getResponseBody();
        assertNotNull(filterByBody);
        assertEquals(filterByBody.posts().get(0).title(), postOne.title());

        PageOfPosts filterBySearchAndCategory = client.get().uri("/posts?searchInput=First&category=HOUSING")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).exchangeSuccessfully()
                .expectBody(PageOfPosts.class).returnResult().getResponseBody();
        assertNotNull(filterBySearchAndCategory);
        assertTrue(filterBySearchAndCategory.posts().isEmpty());

        PageOfPosts sortByRecentAscending = client.get().uri("/posts?sort=createdAt,asc")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).exchangeSuccessfully()
                .expectBody(PageOfPosts.class).returnResult().getResponseBody();
        assertNotNull(sortByRecentAscending);
        // The first post should be at the top of the list because it is older
        assertEquals(sortByRecentAscending.posts().get(0).title(), postOne.title());

        PageOfPosts sortByLikes = client.get().uri("/posts?sort=likes,desc")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).exchangeSuccessfully()
                .expectBody(PageOfPosts.class).returnResult().getResponseBody();
        assertNotNull(sortByLikes);
        assertEquals(sortByLikes.posts().get(0).title(), postTwo.title());
    }
}

