package com.example.nsu_backend.controllers;

import com.example.nsu_backend.dto.UpdateUsernameRequest;
import com.example.nsu_backend.dto.UserDetails;
import com.example.nsu_backend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("name")
    public UserDetails updateUsername(@RequestBody UpdateUsernameRequest request) {
        return userService.updateUsername(request);
    }

    @PostMapping("profile-icon")
    public UserDetails updateProfileIcon(@RequestParam("file") MultipartFile file) {
        return userService.updateProfileIcon(file);
    }
}
