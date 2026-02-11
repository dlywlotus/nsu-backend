package com.example.nsu_backend.repositories;

import com.example.nsu_backend.entities.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    void deleteByUserIdAndPostId(UUID userId, UUID postId);
}
