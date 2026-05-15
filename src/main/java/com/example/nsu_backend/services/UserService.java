package com.example.nsu_backend.services;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final S3Client s3Client;

    public void saveUser(SignUpRequest request) {
        userRepository.findByUsername(request.username()).ifPresent(user -> {
            throw new ApiException("Account with the specified username already exists");
        });

        User user = User.builder()
                .username(request.username())
                .encryptedPassword(passwordEncoder.encode(request.password()))
                .build();
        userRepository.save(user);
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
        return UUID.fromString((String) Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal).orElse(""));
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
        String imageKey = userId.toString();

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

