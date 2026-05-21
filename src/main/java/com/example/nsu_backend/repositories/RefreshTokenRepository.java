package com.example.nsu_backend.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.nsu_backend.dto.RevokedTokenDetails;
import com.example.nsu_backend.entities.RefreshToken;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    void deleteByUserId(UUID userId);

    @Modifying
    @NativeQuery("""
            DELETE FROM refresh_tokens
            WHERE NOW() >= expires_at
            """)
    void cleanUpExpiredTokens();

    @Modifying
    @NativeQuery("""
            UPDATE refresh_tokens
            SET is_revoked = true, revoked_at = NOW()
            WHERE id = :refreshTokenId
            AND is_revoked = false
            AND NOW() <= expires_at
            RETURNING id, user_id, revoked_at""")
    List<RevokedTokenDetails> revokeToken(@Param("refreshTokenId") UUID refreshTokenId);

    @Modifying
    @NativeQuery("""
            UPDATE refresh_tokens
            SET successor_token_id = :successorTokenId
            WHERE id = :revokedTokenId
            AND EXISTS (
                SELECT * FROM refresh_tokens WHERE id = :revokedTokenId
            )
            """)
    void setSuccessorToken(@Param("revokedTokenId") UUID revokedTokenId,
                           @Param("successorTokenId") UUID successorTokenId);
}
