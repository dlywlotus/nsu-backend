package com.example.nsu_backend;

import com.example.nsu_backend.dto.SignInRequest;
import com.example.nsu_backend.dto.SignUpRequest;
import com.example.nsu_backend.dto.UserAuthResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Optional;


@Slf4j
@ActiveProfiles("local")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthIT {
    @LocalServerPort
    private int port;
    private WebTestClient client;
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @BeforeEach
    void beforeEach() {
        redisConnectionFactory.getConnection().serverCommands().flushAll();
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
        client.post().uri("/sign_up").bodyValue(new SignUpRequest("tester", "123123")).exchange();
    }

    @Test
    void givenValidAccessToken_whenSignOut_accessAndRefreshTokenShouldBeInvalidated() {
        EntityExchangeResult<UserAuthResponse> signInResult = client.post().uri("/sign_in").bodyValue(new SignInRequest("tester", "123123"))
                .exchangeSuccessfully().expectBody(UserAuthResponse.class).returnResult();

        String accessToken = Optional.ofNullable(signInResult.getResponseBody()).map(UserAuthResponse::accessToken).orElse("");
        String refreshToken = signInResult.getResponseCookies().toSingleValueMap().get("refresh_token").getValue();

        client.get().uri("/test_secure").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).exchangeSuccessfully();
        client.post().uri("/sign_out").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).cookie("refresh_token", refreshToken)
                .exchangeSuccessfully();

        // Access token is invalidated
        client.get().uri("/test_secure").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).exchange().expectStatus().is4xxClientError();

        // Refresh token is invalidated
        client.post().uri("/refresh_token").cookie("refresh_token", refreshToken).exchange().expectStatus().is4xxClientError();
    }

    @Test
    void givenRefreshTokenStolen_whenUseInvalidatedToken_deleteAllRefreshTokens() {
        EntityExchangeResult<UserAuthResponse> signInResult = client.post().uri("/sign_in").bodyValue(new SignInRequest("tester", "123123"))
                .exchangeSuccessfully().expectBody(UserAuthResponse.class).returnResult();
        String refreshToken = signInResult.getResponseCookies().toSingleValueMap().get("refresh_token").getValue();

        EntityExchangeResult<UserAuthResponse> firstRefreshResult = client.post().uri("/refresh_token").cookie("refresh_token", refreshToken)
                .exchangeSuccessfully().expectBody(UserAuthResponse.class).returnResult();
        String newRefreshToken = firstRefreshResult.getResponseCookies().toSingleValueMap().get("refresh_token").getValue();

        // After the first refresh, the previous refresh token should be invalidated, so the second refresh with the same refresh token will fail
        client.post().uri("/refresh_token").cookie("refresh_token", refreshToken).exchange().expectStatus().is4xxClientError();

        // Using the invalidated refresh token triggers the deletion of all refresh tokens associated with the user
        // Thus, the new refresh token will also not work
        client.post().uri("/refresh_token").cookie("refresh_token", newRefreshToken).exchange().expectStatus().is4xxClientError();
    }


    @Test
    void givenConcurrentSignIn_whenOneDeviceLogout_otherDeviceStaysLoggedIn() {
        // Sign in on two separate devices
        EntityExchangeResult<UserAuthResponse> firstDeviceSignInResult = client.post().uri("/sign_in").bodyValue(new SignInRequest("tester", "123123"))
                .exchangeSuccessfully().expectBody(UserAuthResponse.class).returnResult();
        EntityExchangeResult<UserAuthResponse> secondDeviceSignInResult = client.post().uri("/sign_in").bodyValue(new SignInRequest("tester", "123123"))
                .exchangeSuccessfully().expectBody(UserAuthResponse.class).returnResult();

        String firstDeviceRefreshToken = firstDeviceSignInResult.getResponseCookies().toSingleValueMap().get("refresh_token").getValue();
        String secondDeviceRefreshToken = secondDeviceSignInResult.getResponseCookies().toSingleValueMap().get("refresh_token").getValue();
        String secondDeviceAccessToken = Optional.ofNullable(secondDeviceSignInResult.getResponseBody()).map(UserAuthResponse::accessToken).orElse("");

        // Refresh the access and refresh tokens for the first device
        client.post().uri("/refresh_token").cookie("refresh_token", firstDeviceRefreshToken).exchangeSuccessfully();

        // Access token of the second device is still valid
        client.get().uri("/test_secure").header(HttpHeaders.AUTHORIZATION, "Bearer " + secondDeviceAccessToken).exchangeSuccessfully();

        // Refresh token of the second device is still valid
        client.post().uri("/refresh_token").cookie("refresh_token", secondDeviceRefreshToken).exchangeSuccessfully();
    }
}
