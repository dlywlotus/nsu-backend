package com.example.nsu_backend.repositories;

import com.example.nsu_backend.entities.Comment;
import org.springframework.data.repository.CrudRepository;

public interface CommentRepository extends CrudRepository<Comment, Long> {
}
