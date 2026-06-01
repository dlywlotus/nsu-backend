package com.example.nsu_backend.services;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.nsu_backend.dto.SignInRequest;
import com.example.nsu_backend.dto.SignUpRequest;
import com.example.nsu_backend.dto.UpdateUsernameRequest;
import com.example.nsu_backend.dto.UserDetails;
import com.example.nsu_backend.entities.User;
import com.example.nsu_backend.exceptions.ApiException;
import com.example.nsu_backend.exceptions.UserLoginException;
import com.example.nsu_backend.mappers.UserMapper;
import com.example.nsu_backend.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    public static final UUID UNAUTHENTICATED_USER_ID = UUID.randomUUID();
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final S3Client s3Client;

    public UserDetails getUser(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException("User not found"));
        return userMapper.userToUserDto(user);
    }

    public UserDetails saveUser(SignUpRequest request) {
        userRepository.findByUsername(request.username()).ifPresent(user -> {
            throw new ApiException("Account with the specified username already exists");
        });

        User user = User.builder()
                .username(request.username())
                .encryptedPassword(passwordEncoder.encode(request.password()))
                .build();
        User newUser = userRepository.save(user);
        return new UserDetails(newUser.getId(), newUser.getUsername(), newUser.getProfileIconImageKey());
    }

    public User validateUserDetails(SignInRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new UserLoginException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getEncryptedPassword())) {
            throw new UserLoginException("Invalid username or password");
        }

        return user;
    }


    public UUID getCurrentUserId() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(authentication -> authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken))
                .map(Authentication::getPrincipal)
                .map(Object::toString)
                .map(UUID::fromString)
                .orElse(UNAUTHENTICATED_USER_ID);
    }

    @Transactional
    public UserDetails updateUsername(UpdateUsernameRequest request) {
        User user = userRepository.findById(getCurrentUserId())
                .orElseThrow(() -> new ApiException("User not found!"));
        user.setUsername(request.username());
        return userMapper.userToUserDto(userRepository.save(user));
    }


    @Transactional
    public UserDetails updateProfileIcon(MultipartFile file) {

        UUID userId = getCurrentUserId();
        String imageKey = UUID.randomUUID().toString();

        try {
            // 1. Upload to S3 first
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket("profile-icons")
                            .key(imageKey)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            // 2. Update DB
            User user = userRepository.findById(userId).orElseThrow(() -> new ApiException("User not found"));
            user.setProfileIconImageKey(imageKey);
            User saved = userRepository.save(user);
            return userMapper.userToUserDto(saved);

        } catch (Exception ex) {
            try {
                s3Client.deleteObject(
                        DeleteObjectRequest.builder()
                                .bucket("profile-icons")
                                .key(imageKey)
                                .build()
                );
            } catch (Exception cleanupEx) {
                log.error("Failed to clean up profile icon image upload");
            }

            throw new RuntimeException("Failed to update profile icon", ex);
        }
    }
}

