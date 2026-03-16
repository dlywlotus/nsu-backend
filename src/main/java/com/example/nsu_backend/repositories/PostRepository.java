package com.example.nsu_backend.repositories;

import com.example.nsu_backend.entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    @Query("Select p FROM Post p WHERE p.author.id = :authorId AND p.id = :postId")
    Optional<Post> findByPostAndAuthorId(UUID postId, UUID authorId);
}
