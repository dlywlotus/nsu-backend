package com.example.nsu_backend.services;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.nsu_backend.dto.SignUpRequest;
import com.example.nsu_backend.entities.User;
import com.example.nsu_backend.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User saveUser(SignUpRequest request) {
        User user = User.builder()
                .username(request.username())
                .encryptedPassword(passwordEncoder.encode(request.password()))
                .build();
        return userRepository.save(user);
    }
}