package com.example.nsu_backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    private String username;

    @Column(nullable = false)
    private String encryptedPassword;

    @OneToMany(mappedBy = "author")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Post> posts;

    @OneToMany(mappedBy = "author")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Comment> comments;
}