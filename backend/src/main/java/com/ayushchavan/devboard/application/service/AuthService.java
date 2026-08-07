package com.ayushchavan.devboard.application.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ayushchavan.devboard.application.dto.auth.LoginRequest;
import com.ayushchavan.devboard.application.dto.auth.LoginResponse;
import com.ayushchavan.devboard.application.dto.user.UserResponse;
import com.ayushchavan.devboard.application.exception.AuthenticationException;
import com.ayushchavan.devboard.domain.entity.User;
@Service
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserService userService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {

        User user = userService.findByEmail(request.email())
                .orElseThrow(() ->
                        new AuthenticationException(
                        "Invalid email or password"
                        )
                );

        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )) {
            throw new AuthenticationException(
                "Invalid email or password"
                );
        }

        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail()
        );

        return new LoginResponse(
                token,
                UserResponse.from(user)
        );
    }
}