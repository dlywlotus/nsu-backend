package com.example.nsu_backend.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.nsu_backend.entities.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    @Query("Select p FROM Post p WHERE p.author.id = :authorId AND p.id = :postId")
    Optional<Post> findByPostAndAuthorId(UUID postId, UUID authorId);

    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount - 1 WHERE p.id = :postId")
    void decrementLikeCount(UUID postId);

    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :postId")
    void incrementLikeCount(UUID postId);

}
