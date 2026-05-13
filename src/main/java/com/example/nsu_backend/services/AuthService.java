package com.example.nsu_backend.services;

import com.example.nsu_backend.dto.CookieAuthResponse;
import com.example.nsu_backend.dto.UserAuthResponse;
import com.example.nsu_backend.exceptions.ApiException;
import com.example.nsu_backend.exceptions.TokenRefreshException;
import com.example.nsu_backend.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Duration;
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
    public final static String USER_ID_HASH_KEY = "userId";
    public final static String IS_REVOKED_HASH_KEY = "isRevoked";

    public final static String JWT_USER_ID_CLAIMS = "userId";

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtProperties jwtProperties;

    public CookieAuthResponse signIn(UUID userId, String refreshToken) {
        invalidateRefreshToken(refreshToken);
        ResponseCookie newRefreshTokenCookie = createRefreshTokenCookie(userId.toString());
        UserAuthResponse userAuthResponse = new UserAuthResponse(createAccessToken(userId.toString()), userId);
        return new CookieAuthResponse(newRefreshTokenCookie, userAuthResponse);
    }

    public void signOut(String refreshToken) {
        invalidateRefreshToken(refreshToken);
    }

    public CookieAuthResponse handleTokenRefresh(String refreshToken) {
        if (!redisTemplate.hasKey(refreshToken)) {
            throw new TokenRefreshException("Refresh token has expired");
        }

        if (isRevoked(refreshToken)) {
            log.error("MALICIOUS REFRESH TOKEN USAGE DETECTED");

            deleteAllRefreshTokens(redisTemplate.<String, String>opsForHash().get(refreshToken, USER_ID_HASH_KEY));
            throw new TokenRefreshException("Refresh token has expired");
        }

        invalidateRefreshToken(refreshToken);
        String userId = redisTemplate.<String, String>opsForHash().get(refreshToken, USER_ID_HASH_KEY);
        ResponseCookie newRefreshTokenCookie = createRefreshTokenCookie(userId);
        UserAuthResponse userAuthResponse = new UserAuthResponse(createAccessToken(userId), UUID.fromString(userId));
        return new CookieAuthResponse(newRefreshTokenCookie, userAuthResponse);
    }

    public void validateJwt(String accessToken) {
        Claims payload = Jwts.parser().verifyWith(jwtProperties.getDecodedSecretKey())
                .build().parseSignedClaims(accessToken).getPayload();
        Object userId = payload.get(JWT_USER_ID_CLAIMS);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userId, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    public String createAccessToken(String userId) {
        Instant expirationInstant = Instant.now().plus(15, ChronoUnit.MINUTES);
        return Jwts.builder()
                .claims(Map.of(
                        JWT_USER_ID_CLAIMS, userId
                ))
                .expiration(Date.from(expirationInstant))
                .signWith(jwtProperties.getDecodedSecretKey()).compact();
    }

    public ResponseCookie createRefreshTokenCookie(String userId) {
        String refreshToken = REFRESH_TOKEN_PREFIX + UUID.randomUUID();

        redisTemplate.opsForHash().put(refreshToken, IS_REVOKED_HASH_KEY, "false");
        redisTemplate.opsForHash().put(refreshToken, USER_ID_HASH_KEY, userId);

        redisTemplate.expire(refreshToken, 30, TimeUnit.DAYS);
        redisTemplate.opsForSet().add(REFRESH_TOKEN_SET_PREFIX + userId, refreshToken);
        return ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(30))
                .sameSite("Lax") // Helps mitigate CSRF
                .build();
    }

    public void invalidateRefreshToken(String refreshToken) {
        redisTemplate.opsForHash().put(refreshToken, IS_REVOKED_HASH_KEY, "true");
    }

    public void deleteAllRefreshTokens(String userId) {
        Set<Object> refreshTokens = redisTemplate.opsForSet().members(REFRESH_TOKEN_SET_PREFIX + userId);
        for (Object refreshToken : refreshTokens) {
            redisTemplate.delete((String) refreshToken);
        }
        redisTemplate.delete(REFRESH_TOKEN_SET_PREFIX + userId);
    }

    public boolean isRevoked(String refreshToken) {
        String isRevokedString = redisTemplate.<String, String>opsForHash().get(refreshToken, IS_REVOKED_HASH_KEY);
        if (isRevokedString == null) {
            throw new ApiException("The key, " + refreshToken + ", or it's hashkey, `isRevoked`, does not exist");
        }
        return isRevokedString.equals("true");
    }

    public UUID getCurrentUserId() {
        return UUID.fromString((String) Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal).orElse(""));
    }
}
