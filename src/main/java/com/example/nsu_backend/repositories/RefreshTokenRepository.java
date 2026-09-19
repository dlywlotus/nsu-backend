package com.example.nsu_backend.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.stereotype.Repository;

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
}
