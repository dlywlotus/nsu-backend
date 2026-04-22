package com.example.nsu_backend.services;

import com.example.nsu_backend.exceptions.ApiException;
import com.example.nsu_backend.properties.JwtProperties;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthService {
    public final static String REFRESH_TOKEN_PREFIX = "refreshToken:";
    public final static String REFRESH_TOKEN_SET_PREFIX = "refreshTokensFor:";
    public final static String ACCESS_TOKEN_VERSION_PREFIX = "accessTokenVersionFor:";
    public final static String BLACK_LISTED_TOKEN_PREFIX = "blackListedAccessToken:";
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtProperties jwtProperties;

    public String createAccessToken(String userId) {
        Instant expirationInstant = Instant.now().plus(15, ChronoUnit.MINUTES);
        Integer tokenVersion = getAccessTokenVersion(userId);
        return Jwts.builder()
                .claims(Map.of(
                        "userId", userId,
                        "version", tokenVersion
                ))
                .expiration(Date.from(expirationInstant))
                .signWith(jwtProperties.getDecodedSecretKey()).compact();
    }

    public ResponseCookie createRefreshTokenCookie(String userId) {
        String refreshToken = REFRESH_TOKEN_PREFIX + UUID.randomUUID();

        redisTemplate.opsForHash().put(refreshToken, "isRevoked", "false");
        redisTemplate.opsForHash().put(refreshToken, "userId", userId);
        redisTemplate.expire(refreshToken, 30, TimeUnit.DAYS);
        redisTemplate.opsForSet().add(REFRESH_TOKEN_SET_PREFIX + userId, refreshToken);
        return ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(3600)
                .sameSite("Lax") // Helps mitigate CSRF
                .build();
    }

    public void invalidateAllAccessTokens(String userId) {
        Integer previousAccessTokenVersion = getAccessTokenVersion(userId);
        redisTemplate.opsForValue().set(ACCESS_TOKEN_VERSION_PREFIX + userId, previousAccessTokenVersion + 1);
    }

    public void invalidateRefreshToken(String refreshToken) {
        redisTemplate.opsForHash().put(refreshToken, "isRevoked", "true");
    }

    public void deleteAllRefreshTokens(String userId) {
        Set<Object> refreshTokens = redisTemplate.opsForSet().members(REFRESH_TOKEN_SET_PREFIX + userId);
        for (Object refreshToken : refreshTokens) {
            redisTemplate.delete((String) refreshToken);
        }
        redisTemplate.delete(REFRESH_TOKEN_SET_PREFIX + userId);
    }

    public void blacklistAccessToken(String accessToken) {
        String blackListTokenKey = BLACK_LISTED_TOKEN_PREFIX + accessToken;
        redisTemplate.opsForValue().set(blackListTokenKey, 0);
        redisTemplate.expire(blackListTokenKey, 15, TimeUnit.MINUTES);
    }

    public String removeBearerPrefix(String authorizationHeader) {
        return authorizationHeader.substring(7);
    }

    public Integer getAccessTokenVersion(String userId) {
        return (Integer) Optional.ofNullable(
                redisTemplate.opsForValue().get(ACCESS_TOKEN_VERSION_PREFIX + userId)).orElse(1);
    }

    public boolean has(String refreshToken) {
        return redisTemplate.hasKey(refreshToken);
    }

    public boolean isRevoked(String refreshToken) {

        String isRevokedString = redisTemplate.<String, String>opsForHash().get(refreshToken, "isRevoked");
        if (isRevokedString == null) {
            throw new ApiException("The key, " + refreshToken + ", or it's hashkey, `isRevoked`, does not exist");
        }
        return isRevokedString.equals("true");
    }

    public String getUserIdFrom(String refreshToken) {
        return redisTemplate.<String, String>opsForHash().get(refreshToken, "userId");
    }

    public UUID getCurrentUserId() {
        return UUID.fromString((String) Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal).orElse(""));
    }
}
