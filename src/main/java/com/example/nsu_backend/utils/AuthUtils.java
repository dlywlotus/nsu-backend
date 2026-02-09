package com.example.nsu_backend.utils;

import com.example.nsu_backend.dto.AuthTokensResponse;
import com.example.nsu_backend.properties.JwtProperties;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class AuthUtils {
    public final static String REFRESH_TOKEN_PREFIX = "refreshToken:";
    public final static String REFRESH_TOKEN_SET_PREFIX = "refreshTokensFor:";
    public final static String ACCESS_TOKEN_VERSION_PREFIX = "accessTokenVersionFor:";
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtProperties jwtProperties;

    public AuthTokensResponse createJwtTokens(UUID userId, String deviceId) {
        //Create access token
        Instant expirationInstant = Instant.now().plus(15, ChronoUnit.MINUTES);
        ZonedDateTime expirationDateTime = ZonedDateTime.ofInstant(expirationInstant, ZoneId.ofOffset("UTC", ZoneOffset.ofHours(0)));
        Integer tokenVersion = getGlobalAccessTokenVersion(userId);
        String accessToken = Jwts.builder()
                .claims(Map.of(
                        "userId", userId.toString(),
                        "version", tokenVersion
                ))
                .expiration(Date.from(expirationInstant))
                .signWith(jwtProperties.getDecodedSecretKey()).compact();

        //Create refresh token
        String refreshTokenString = REFRESH_TOKEN_PREFIX + UUID.randomUUID();
        Map<String, String> refreshTokenValue = Map.of(
                "userId", userId.toString(),
                "deviceId", deviceId,
                "isRevoked", "false"
        );
        redisTemplate.opsForHash().putAll(refreshTokenString, refreshTokenValue);
        redisTemplate.expire(refreshTokenString, 30, TimeUnit.DAYS);
        redisTemplate.opsForSet().add(REFRESH_TOKEN_SET_PREFIX + userId, refreshTokenString);
        return new AuthTokensResponse(accessToken, refreshTokenString, expirationDateTime.toString());
    }

    public void invalidateAllAccessTokens(UUID userId) {
        Integer previousAccessTokenVersion = getGlobalAccessTokenVersion(userId);
        redisTemplate.opsForValue().set(ACCESS_TOKEN_VERSION_PREFIX + userId, previousAccessTokenVersion + 1);
    }

    public void invalidateOldRefreshToken(UUID userId, String deviceToInvalidate) {
        Set<Object> refreshTokens = redisTemplate.opsForSet().members(REFRESH_TOKEN_SET_PREFIX + userId.toString());
        for (Object refreshToken : refreshTokens) {
            String refreshTokenString = (String) refreshToken;
            String deviceId = (String) redisTemplate.opsForHash().get(refreshTokenString, "deviceId");
            if (Objects.equals(deviceId, deviceToInvalidate)) {
                redisTemplate.opsForHash().put(refreshTokenString, "isRevoked", "true");
                //Override TTL of old refresh token to be only 24 hours - more than sufficient to catch malicious attacks
                redisTemplate.expire(refreshTokenString, 24, TimeUnit.HOURS);
            }
            redisTemplate.opsForSet().remove("refreshTokensFor" + userId, refreshToken);
        }
    }

    public void invalidateAllRefreshTokens(UUID userId) {
        Set<Object> refreshTokens = redisTemplate.opsForSet().members(REFRESH_TOKEN_SET_PREFIX + userId.toString());
        for (Object refreshToken : refreshTokens) {
            redisTemplate.delete((String) refreshToken);
        }
        redisTemplate.delete(REFRESH_TOKEN_SET_PREFIX + userId);
    }

    public String removeBearerPrefix(String authorizationHeader) {
        return authorizationHeader.substring(7);
    }

    public Integer getGlobalAccessTokenVersion(UUID userId) {
        return (Integer) Optional.ofNullable(
                redisTemplate.opsForValue().get(ACCESS_TOKEN_VERSION_PREFIX + userId.toString())).orElse(1);
    }

    public String getCurrentUserId() {
        return (String) Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal).orElse("");
    }
}
