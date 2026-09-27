package com.example.nsu_backend.services;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import com.example.nsu_backend.entities.RefreshToken;
import com.example.nsu_backend.exceptions.TokenRefreshException;
import com.example.nsu_backend.repositories.RefreshTokenRepository;
import com.example.nsu_backend.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshToken getToken(UUID refreshTokenId) {
        return refreshTokenRepository.findById(refreshTokenId).orElseThrow(() -> new TokenRefreshException("Refresh token expired"));
    }

    public UUID createToken(UUID userId) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(userRepository.getReferenceById(userId))
                .expiresAt(OffsetDateTime.now().plusDays(30))
                .build();
        RefreshToken savedRefreshToken = refreshTokenRepository.save(refreshToken);
        return savedRefreshToken.getId();
    }

    public void removeToken(UUID tokenId) {
        refreshTokenRepository.deleteById(tokenId);
    }

    public void cleanUpExpiredTokens() {
        refreshTokenRepository.cleanUpExpiredTokens();
    }

    public ResponseCookie generateCookie(UUID refreshTokenId) {
        return ResponseCookie.from("refresh_token", refreshTokenId.toString())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(30))
                .sameSite("None")
                .build();
    }
}
