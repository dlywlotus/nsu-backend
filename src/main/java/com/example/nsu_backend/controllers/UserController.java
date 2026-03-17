package com.example.nsu_backend.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.nsu_backend.dto.UpdateProfileRequest;
import com.example.nsu_backend.dto.UserDetails;
import com.example.nsu_backend.services.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserDetails updateProfile(@RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(request);
    }

}
