package com.ayushchavan.devboard.application.service;

import com.ayushchavan.devboard.domain.entity.User;
import com.ayushchavan.devboard.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void findById_shouldReturnUserWhenUserExists() {
        UUID userId = UUID.randomUUID();

        User user = new User(
                userId,
                "Ayush Chavan",
                "ayush@example.com",
                "hashed-password",
                Instant.now(),
                Instant.now()
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        Optional<User> result = userService.findById(userId);

        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
        assertEquals("Ayush Chavan", result.get().getName());

        verify(userRepository).findById(userId);
    }

    @Test
    void findById_shouldReturnEmptyWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        Optional<User> result = userService.findById(userId);

        assertTrue(result.isEmpty());

        verify(userRepository).findById(userId);
    }

    @Test
    void findByEmail_shouldReturnUserWhenUserExists() {
        String email = "ayush@example.com";
        UUID userId = UUID.randomUUID();

        User user = new User(
                userId,
                "Ayush Chavan",
                email,
                "hashed-password",
                Instant.now(),
                Instant.now()
        );

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        Optional<User> result = userService.findByEmail(email);

        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());

        verify(userRepository).findByEmail(email);
    }

    @Test
    void existsByEmail_shouldReturnTrueWhenEmailExists() {
        String email = "ayush@example.com";

        when(userRepository.existsByEmail(email))
                .thenReturn(true);

        boolean result = userService.existsByEmail(email);

        assertTrue(result);

        verify(userRepository).existsByEmail(email);
    }
}