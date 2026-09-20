package com.example.nsu_backend.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import com.example.nsu_backend.dto.AuthCodeRequest;
import com.example.nsu_backend.dto.CreateUserRequest;
import com.example.nsu_backend.dto.MessageResponse;
import com.example.nsu_backend.dto.TokenExchangeRequest;
import com.example.nsu_backend.dto.TokenExchangeResponse;
import com.example.nsu_backend.dto.UserAuthResponse;
import com.example.nsu_backend.dto.UserDetails;
import com.example.nsu_backend.entities.RefreshToken;
import com.example.nsu_backend.exceptions.ApiException;
import com.example.nsu_backend.exceptions.TokenRefreshException;
import com.example.nsu_backend.services.AccessTokenService;
import com.example.nsu_backend.services.RefreshTokenService;
import com.example.nsu_backend.services.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthenticationController {
    private final RefreshTokenService refreshTokenService;
    private final AccessTokenService accessTokenService;
    private final UserService userService;
    private final JwtDecoder jwtDecoder;

    @Value("${frontend.server.url}")
    private String frontEndServerUrl;

    @Value("${oidc.google.client_secret}")
    private String clientSecret;

    @PostMapping("/refresh_token")
    public ResponseEntity<UserAuthResponse> handleTokenRefresh(@CookieValue(name = "refresh_token", defaultValue = "") UUID refreshTokenId) {
        RefreshToken refreshToken = refreshTokenService.getToken(refreshTokenId);

        if (OffsetDateTime.now().isAfter(refreshToken.getExpiresAt())) {
            throw new TokenRefreshException("Refresh token expired");
        }

        UUID userId = refreshToken.getUser().getId();
        String accessToken = accessTokenService.createAccessToken(userId);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenId.toString())
                .body(new UserAuthResponse(accessToken, userId));
    }

    @PostMapping("/token")
    public ResponseEntity<UserAuthResponse> exchangeAuthCode(@RequestBody AuthCodeRequest request) {
        TokenExchangeResponse res = RestClient.create("https://oauth2.googleapis.com/token")
                .post()
                .contentType(APPLICATION_JSON)
                .body(new TokenExchangeRequest(request.authCode(), request.clientId(), clientSecret,
                        frontEndServerUrl + "/auth-callback", "authorization_code"))
                .accept(APPLICATION_JSON)
                .retrieve().body(TokenExchangeResponse.class);

        if (res == null) {
            throw new ApiException("Invalid authorization code");
        }

        Jwt jwt = jwtDecoder.decode(res.idToken());
        Map<String, Object> claimsMap = jwt.getClaims();
        String googleSub = claimsMap.get("sub").toString();
        String username = claimsMap.get("name").toString();

        UserDetails userDetails = userService.createUser(new CreateUserRequest(username, googleSub));
        ResponseCookie refreshTokenCookie = refreshTokenService.generateCookie(refreshTokenService.createToken(userDetails.id()));
        String accessToken = accessTokenService.createAccessToken(userDetails.id());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(new UserAuthResponse(accessToken, userDetails.id()));
    }

    @PostMapping("/sign_out")
    public MessageResponse signOut(@CookieValue(name = "refresh_token", defaultValue = "") UUID refreshTokenId) {
        refreshTokenService.removeToken(refreshTokenId);
        return new MessageResponse("Successfully logged out");
    }

    @GetMapping("/test_secure")
    public String testSecureRoute() {
        return "Successfully accessed secure route";
    }
}