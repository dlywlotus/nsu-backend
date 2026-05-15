package com.example.nsu_backend.services;

import com.example.nsu_backend.dto.*;
import com.example.nsu_backend.entities.RefreshToken;
import com.example.nsu_backend.entities.User;
import com.example.nsu_backend.exceptions.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final AccessTokenService accessTokenService;

    public void signUp(SignUpRequest request) {
        userService.saveUser(request);
    }

    public CookieAuthResponse signIn(SignInRequest request) {
        User user = userService.validateUserDetails(request);
        String accessToken = accessTokenService.createAccessToken(user.getId());
        ResponseCookie refreshTokenCookie = refreshTokenService.createToken(user.getId());
        UserAuthResponse userAuthResponse = new UserAuthResponse(accessToken, user.getId());
        return new CookieAuthResponse(refreshTokenCookie, userAuthResponse);
    }

    public void signOut(UUID tokenId) {
        refreshTokenService.removeToken(tokenId);
    }

    @Transactional
    public CookieAuthResponse handleTokenRefresh(UUID refreshTokenId) {
        List<RevokedTokenDetails> revokedTokenList = refreshTokenService.revokeToken(refreshTokenId);
        if (revokedTokenList.isEmpty()) {
            // Token revocation can fail because the token was already deleted, revoked (malicious usage) or expired.
            RefreshToken refreshToken = refreshTokenService.getRefreshToken(refreshTokenId);

            if (OffsetDateTime.now().isAfter(refreshToken.getExpiresAt())) {
                throw new ApiException("Invalid refresh token");
            }

            // If the token was revoked less than 500ms ago, a multi-tab concurrent token refresh is assumed to have happened
            // Else, the token is assumed to have been used maliciously
            // In both cases, the refresh request fails with an exception
            if (OffsetDateTime.now().isAfter(refreshToken.getRevokedAt().plus(500, ChronoUnit.MILLIS))) {
                log.warn("Refresh token reuse detected for user {}", refreshToken.getUser().getId());
                refreshTokenService.deleteAllRefreshTokensForUser(refreshToken.getUser().getId());
            }
            throw new ApiException("Invalid refresh token");

        } else {
            UUID userId = revokedTokenList.get(0).getUserId();
            String accessToken = accessTokenService.createAccessToken(userId);
            ResponseCookie refreshTokenCookie = refreshTokenService.createToken(userId);
            UserAuthResponse userAuthResponse = new UserAuthResponse(accessToken, userId);
            return new CookieAuthResponse(refreshTokenCookie, userAuthResponse);
        }
    }
}
