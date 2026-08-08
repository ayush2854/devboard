package com.ayushchavan.devboard.presentation.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ayushchavan.devboard.application.dto.user.ChangePasswordRequest;
import com.ayushchavan.devboard.application.dto.user.CreateUserRequest;
import com.ayushchavan.devboard.application.dto.user.UpdateUserRequest;
import com.ayushchavan.devboard.application.dto.user.UserResponse;
import com.ayushchavan.devboard.application.service.UserService;
import com.ayushchavan.devboard.domain.entity.User;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @RequestBody CreateUserRequest request
    ) {
        User user = userService.createUser(
                request.getName(),
                request.getEmail(),
                request.getPassword()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserResponse.from(user));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();

        User user = userService.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            Authentication authentication,
            @RequestBody UpdateUserRequest request
    ) {
        UUID userId = (UUID) authentication.getPrincipal();

        User user = userService.updateProfile(
                userId,
                request.getName(),
                request.getEmail()
        );

        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            @RequestBody ChangePasswordRequest request
    ) {
        UUID userId = (UUID) authentication.getPrincipal();

        userService.changePassword(
                userId,
                request.getCurrentPassword(),
                request.getNewPassword()
        );

        return ResponseEntity.noContent().build();
    }
}
