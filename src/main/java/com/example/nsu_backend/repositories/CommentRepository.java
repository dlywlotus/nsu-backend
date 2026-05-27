package com.example.nsu_backend.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.stereotype.Repository;

import com.example.nsu_backend.entities.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findByIdAndAuthorId(Long id, UUID authorId);

    List<Comment> findByPostId(UUID postId);

    @Modifying
    @NativeQuery("""
            DELETE FROM comments c
            WHERE c.id = :id
            OR parent_comment_id = :id
            """)
    int deleteCommentTree(Long id);
}
