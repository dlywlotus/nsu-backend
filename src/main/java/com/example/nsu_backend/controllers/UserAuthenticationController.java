package com.example.nsu_backend.controllers;

import com.example.nsu_backend.dto.LogoutRequest;
import com.example.nsu_backend.dto.TokenRefreshRequest;
import com.example.nsu_backend.dto.UserAuthRequest;
import com.example.nsu_backend.dto.UserAuthResponse;
import com.example.nsu_backend.entities.User;
import com.example.nsu_backend.exceptions.TokenRefreshException;
import com.example.nsu_backend.exceptions.UserLoginException;
import com.example.nsu_backend.services.UserService;
import com.example.nsu_backend.utils.AuthUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserAuthenticationController {
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AuthUtils authUtils;

    @PostMapping("/sign_up")
    public UserAuthResponse signUp(@Valid @RequestBody UserAuthRequest request) {
        User newUser = userService.saveUser(request);
        return new UserAuthResponse(authUtils.createJwtTokens(newUser.getId(), request.deviceId()), newUser.getId());
    }

    @PostMapping("/sign_in")
    public UserAuthResponse signIn(@Valid @RequestBody UserAuthRequest request) {
        User user = userService.getUserByUsername(request.username())
                .orElseThrow(() -> new UserLoginException("User does not exist"));

        if (!passwordEncoder.matches(request.password(), user.getEncryptedPassword())) {
            throw new UserLoginException("Invalid username or password");
        }

        authUtils.invalidateOldRefreshToken(user.getId(), request.deviceId());
        return new UserAuthResponse(authUtils.createJwtTokens(user.getId(), request.deviceId()), user.getId());
    }

    @PostMapping("/sign_out")
    public Map<String, String> signOut(@RequestHeader("Authorization") String authorizationHeader, @Valid @RequestBody LogoutRequest request) {
        //Blacklist existing access token
        String jws = authUtils.removeBearerPrefix(authorizationHeader);
        redisTemplate.opsForValue().set("blackListedAccessToken:" + jws, 0);
        redisTemplate.expire("blackListedAccessToken:" + jws, 15, TimeUnit.MINUTES);

        authUtils.invalidateOldRefreshToken(request.userId(), request.deviceId());
        return Map.of("message", "Successfully logged out");
    }

    @PostMapping("/refresh_token")
    public UserAuthResponse refreshToken(@Valid @RequestBody TokenRefreshRequest request) {

        if (!redisTemplate.hasKey(request.refreshTokenString())) {
            throw new TokenRefreshException("Refresh token has expired");
        }

        if (Objects.equals(redisTemplate.opsForHash().get(request.refreshTokenString(), "isRevoked"), "true")) {
            log.error("MALICIOUS REFRESH TOKEN USAGE DETECTED");
            authUtils.invalidateAllAccessTokens(request.userId());
            authUtils.invalidateAllRefreshTokens(request.userId());
            throw new TokenRefreshException("Refresh token has expired");
        }

        //Invalidate old refresh token then create new access and refresh tokens
        redisTemplate.opsForHash().put(request.refreshTokenString(), "isRevoked", "true");
        return new UserAuthResponse(authUtils.createJwtTokens(request.userId(), request.deviceId()), request.userId());
    }
}