package com.example.nsu_backend.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.nsu_backend.dto.UpdateUsernameRequest;
import com.example.nsu_backend.dto.UserDetails;
import com.example.nsu_backend.services.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public UserDetails getUserDetails() {
        return userService.getUser(userService.getCurrentUserId());
    }

    @PutMapping("name")
    public UserDetails updateUsername(@RequestBody @Valid UpdateUsernameRequest request) {
        return userService.updateUsername(request);
    }

    @PutMapping("profile-icon")
    public UserDetails updateProfileIcon(@RequestParam("file") MultipartFile file) {
        return userService.updateProfileIcon(file);
    }
}
