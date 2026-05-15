package com.example.nsu_backend.services;

import com.example.nsu_backend.dto.RevokedTokenDetails;
import com.example.nsu_backend.entities.RefreshToken;
import com.example.nsu_backend.exceptions.ApiException;
import com.example.nsu_backend.repositories.RefreshTokenRepository;
import com.example.nsu_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public RefreshToken getRefreshToken(UUID refreshTokenId) {
        return refreshTokenRepository.findById(refreshTokenId)
                .orElseThrow(() -> new ApiException("Invalid refresh token"));
    }

    public ResponseCookie createToken(UUID userId) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(userRepository.getReferenceById(userId))
                .expiresAt(OffsetDateTime.now().plusDays(30))
                .build();
        RefreshToken savedRefreshToken = refreshTokenRepository.save(refreshToken);
        return generateCookie(savedRefreshToken.getId());
    }

    public void removeToken(UUID tokenId) {
        UUID currentUserId = userService.getCurrentUserId();
        RefreshToken refreshToken = refreshTokenRepository.findById(tokenId)
                .orElseThrow(() -> new ApiException("Invalid refresh token provided"));

        if (!refreshToken.getUser().getId().equals(currentUserId)) {
            throw new ApiException("Invalid refresh token provided");
        }

        refreshTokenRepository.deleteById(tokenId);
    }

    public List<RevokedTokenDetails> revokeToken(UUID refreshTokenId) {
        return refreshTokenRepository.revokeToken(refreshTokenId);
    }

    public void deleteAllRefreshTokensForUser(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    public ResponseCookie generateCookie(UUID refreshTokenId) {
        return ResponseCookie.from("refresh_token", refreshTokenId.toString())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(30))
                .sameSite("Lax") // Helps mitigate CSRF
                .build();
    }

}
