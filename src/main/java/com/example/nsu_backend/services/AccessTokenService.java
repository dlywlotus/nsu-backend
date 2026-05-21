package com.example.nsu_backend.services;

import com.example.nsu_backend.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessTokenService {
    public final static String JWT_USER_ID_CLAIMS = "userId";
    private final JwtProperties jwtProperties;

    public void validateAccessToken(String accessToken) {
        Claims payload = Jwts.parser().verifyWith(jwtProperties.getDecodedSecretKey())
                .build().parseSignedClaims(accessToken).getPayload();
        Object userId = payload.get(JWT_USER_ID_CLAIMS);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userId, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    public String createAccessToken(UUID userId) {
        Instant expirationInstant = Instant.now().plus(15, ChronoUnit.MINUTES);
        return Jwts.builder()
                .claims(Map.of(
                        JWT_USER_ID_CLAIMS, userId
                ))
                .expiration(Date.from(expirationInstant))
                .signWith(jwtProperties.getDecodedSecretKey()).compact();
    }
}
