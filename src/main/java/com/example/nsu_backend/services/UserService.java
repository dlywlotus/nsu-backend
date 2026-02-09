package com.example.nsu_backend.services;

import com.example.nsu_backend.dto.UserAuthRequest;
import com.example.nsu_backend.entities.User;
import com.example.nsu_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User saveUser(UserAuthRequest userAuthRequest) {
        User user = User.builder()
                .username(userAuthRequest.username())
                .encryptedPassword(passwordEncoder.encode(userAuthRequest.password()))
                .build();
        return userRepository.save(user);
    }
}