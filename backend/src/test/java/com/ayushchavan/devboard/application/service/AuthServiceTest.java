package com.ayushchavan.devboard.application.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ayushchavan.devboard.application.dto.auth.LoginRequest;
import com.ayushchavan.devboard.application.dto.auth.LoginResponse;
import com.ayushchavan.devboard.application.exception.AuthenticationException;
import com.ayushchavan.devboard.domain.entity.User;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(
                UUID.randomUUID(),
                "Ayush Chavan",
                "ayush@example.com",
                "hashed-password",
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    void login_shouldReturnTokenAndUser_whenCredentialsAreValid() {

        LoginRequest request = new LoginRequest(
                "ayush@example.com",
                "password123"
        );

        when(userService.findByEmail("ayush@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "password123",
                "hashed-password"
        )).thenReturn(true);

        when(jwtService.generateToken(
                user.getId(),
                user.getEmail()
        )).thenReturn("test-jwt-token");

        LoginResponse response = authService.login(request);

        assertEquals("test-jwt-token", response.token());
	assertEquals(user.getId(), response.user().getId());
	assertEquals(user.getEmail(), response.user().getEmail());

        verify(userService).findByEmail("ayush@example.com");
        verify(passwordEncoder).matches(
                "password123",
                "hashed-password"
        );
        verify(jwtService).generateToken(
                user.getId(),
                user.getEmail()
        );
    }

    @Test
    void login_shouldReject_whenEmailDoesNotExist() {

        LoginRequest request = new LoginRequest(
                "unknown@example.com",
                "password123"
        );

        when(userService.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        AuthenticationException exception =
        assertThrows(
                AuthenticationException.class,
                () -> authService.login(request)
        );

        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );
    }

    @Test
    void login_shouldReject_whenPasswordIsIncorrect() {

        LoginRequest request = new LoginRequest(
                "ayush@example.com",
                "wrong-password"
        );

        when(userService.findByEmail("ayush@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrong-password",
                "hashed-password"
        )).thenReturn(false);

        AuthenticationException exception =
        assertThrows(
                AuthenticationException.class,
                () -> authService.login(request)
        );

        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );
    }
}