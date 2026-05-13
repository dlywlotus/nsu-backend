package com.example.nsu_backend.controllers;

import com.example.nsu_backend.dto.*;
import com.example.nsu_backend.entities.User;
import com.example.nsu_backend.exceptions.UserLoginException;
import com.example.nsu_backend.services.AuthService;
import com.example.nsu_backend.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthenticationController {
    private final UserService userService;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/sign_up")
    public MessageResponse signUp(@Valid @RequestBody SignUpRequest request) {
        userService.saveUser(request);
        return new MessageResponse("User has signed up successfully");
    }

    @PostMapping("/sign_in")
    public ResponseEntity<UserAuthResponse> signIn(@Valid @RequestBody SignInRequest request,
                                                   @CookieValue(name = "refresh_token", defaultValue = "") String refreshToken) {
        User user = userService.getUserByUsername(request.username())
                .orElseThrow(() -> new UserLoginException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getEncryptedPassword())) {
            throw new UserLoginException("Invalid username or password");
        }
        CookieAuthResponse res = authService.signIn(user.getId(), refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, res.cookie().toString())
                .body(res.authResponse());
    }

    @PostMapping("/sign_out")
    public MessageResponse signOut(@CookieValue(name = "refresh_token", defaultValue = "") String refreshToken) {
        authService.signOut(refreshToken);
        return new MessageResponse("Successfully logged out");
    }

    @PostMapping("/refresh_token")
    public ResponseEntity<UserAuthResponse> handleTokenRefresh(@CookieValue(name = "refresh_token", defaultValue = "") String refreshToken) {
        CookieAuthResponse res = authService.handleTokenRefresh(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, res.cookie().toString()).body(res.authResponse());
    }

    @GetMapping("/test_secure")
    public String testSecureRoute() {
        return "Successfully accessed secure route";
    }
}