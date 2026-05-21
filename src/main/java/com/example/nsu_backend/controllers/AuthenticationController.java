package com.example.nsu_backend.controllers;

import com.example.nsu_backend.dto.*;
import com.example.nsu_backend.services.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/sign_up")
    public MessageResponse signUp(@Valid @RequestBody SignUpRequest request) {
        authenticationService.signUp(request);
        return new MessageResponse("User has signed up successfully");
    }

    @PostMapping("/sign_in")
    public ResponseEntity<UserAuthResponse> signIn(@Valid @RequestBody SignInRequest request) {
        CookieAuthResponse response = authenticationService.signIn(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, response.cookie().toString())
                .body(response.userAuthResponse());
    }

    @PostMapping("/sign_out")
    public MessageResponse signOut(@CookieValue(name = "refresh_token", defaultValue = "") UUID refreshTokenId) {
        authenticationService.signOut(refreshTokenId);
        return new MessageResponse("Successfully logged out");
    }

    @PostMapping("/refresh_token")
    public ResponseEntity<UserAuthResponse> handleTokenRefresh(@CookieValue(name = "refresh_token", defaultValue = "") UUID refreshTokenId) {
        CookieAuthResponse res = authenticationService.handleTokenRefresh(refreshTokenId);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, res.cookie().toString()).body(res.userAuthResponse());
    }

    @GetMapping("/test_secure")
    public String testSecureRoute() {
        return "Successfully accessed secure route";
    }
}