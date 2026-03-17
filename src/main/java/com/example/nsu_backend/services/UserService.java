package com.example.nsu_backend.services;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.nsu_backend.dto.SignUpRequest;
import com.example.nsu_backend.dto.UpdateProfileRequest;
import com.example.nsu_backend.dto.UserDetails;
import com.example.nsu_backend.entities.User;
import com.example.nsu_backend.exceptions.UserLoginException;
import com.example.nsu_backend.mappers.UserMapper;
import com.example.nsu_backend.repositories.UserRepository;
import com.example.nsu_backend.utils.AuthUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthUtils authUtils;
    private final UserMapper userMapper;

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void saveUser(SignUpRequest request) {
        User user = User.builder()
                .username(request.username())
                .encryptedPassword(passwordEncoder.encode(request.password()))
                .build();
        userRepository.save(user);
    }

    public UserDetails updateProfile(UpdateProfileRequest request) {
        User user = userRepository.findById(authUtils.getCurrentUserId())
                .orElseThrow(() -> new UserLoginException("Please log in first!"));
        User updatedUser = User.builder()
                .id(user.getId())
                .username(Optional.ofNullable(request.username()).orElse(user.getUsername()))
                .encryptedPassword(user.getEncryptedPassword())
                .profileIconUrl(Optional.ofNullable(request.profileIconUrl()).orElse(user.getProfileIconUrl()))
                .build();
        return userMapper.userToUserDto(userRepository.save(updatedUser));
    }

}