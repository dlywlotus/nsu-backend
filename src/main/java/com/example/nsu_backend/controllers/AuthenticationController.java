package com.example.nsu_backend.controllers;

import com.example.nsu_backend.dto.SignInRequest;
import com.example.nsu_backend.dto.SignUpRequest;
import com.example.nsu_backend.dto.UserAuthResponse;
import com.example.nsu_backend.entities.User;
import com.example.nsu_backend.exceptions.TokenRefreshException;
import com.example.nsu_backend.exceptions.UserLoginException;
import com.example.nsu_backend.services.AuthService;
import com.example.nsu_backend.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthenticationController {
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/sign_up")
    public Map<String, String> signUp(@Valid @RequestBody SignUpRequest request) {
        userService.saveUser(request);
        log.info(">>>>>>>>>>>>>>>>>>>> Sign up method called");
        return Map.of("message", "User has signed up successfully");
    }

    @PostMapping("/sign_in")
    public ResponseEntity<UserAuthResponse> signIn(@Valid @RequestBody SignInRequest request,
                                                   @CookieValue(name = "refresh_token", defaultValue = "") String refreshToken) {
        User user = userService.getUserByUsername(request.username())
                .orElseThrow(() -> new UserLoginException("User does not exist"));

        if (!passwordEncoder.matches(request.password(), user.getEncryptedPassword())) {
            throw new UserLoginException("Invalid username or password");
        }

        authService.invalidateRefreshToken(refreshToken);
        ResponseCookie newRefreshTokenCookie = authService.createRefreshTokenCookie(user.getId().toString());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newRefreshTokenCookie.toString())
                .body(new UserAuthResponse(authService.createAccessToken(user.getId().toString())));
    }

    @PostMapping("/sign_out")
    public Map<String, String> signOut(@RequestHeader("Authorization") String authorizationHeader,
                                       @CookieValue(name = "refresh_token", defaultValue = "") String refreshToken) {
        // Blacklist existing access token
        String accessToken = authService.removeBearerPrefix(authorizationHeader);
        authService.blacklistAccessToken(accessToken);

        authService.invalidateRefreshToken(refreshToken);
        return Map.of("message", "Successfully logged out");
    }

    @PostMapping("/refresh_token")
    public ResponseEntity<UserAuthResponse> refreshToken(@CookieValue(name = "refresh_token", defaultValue = "") String refreshToken) {
        if (refreshToken.isEmpty() || !authService.has(refreshToken)) {
            throw new TokenRefreshException("Refresh token is invalid or has expired");
        }

        if (authService.isRevoked(refreshToken)) {
            log.error("MALICIOUS REFRESH TOKEN USAGE DETECTED");
            authService.invalidateAllAccessTokens(authService.getUserIdFrom(refreshToken));

            // Refresh tokens are deleted here, NOT INVALIDATED, to prevent a malicious user from spamming the route, disconnecting the user repeatedly
            authService.deleteAllRefreshTokens(authService.getUserIdFrom(refreshToken));
            throw new TokenRefreshException("Refresh token has expired");
        }

        authService.invalidateRefreshToken(refreshToken);
        ResponseCookie newRefreshTokenCookie = authService.createRefreshTokenCookie(authService.getUserIdFrom(refreshToken));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newRefreshTokenCookie.toString())
                .body(new UserAuthResponse(authService.createAccessToken(authService.getUserIdFrom(refreshToken))));
    }

    @GetMapping("/test_secure")
    public String testSecureRoute() {
        return "Successfully accessed secure route";
    }
}