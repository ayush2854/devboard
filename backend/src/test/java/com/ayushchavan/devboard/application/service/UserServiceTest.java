package com.ayushchavan.devboard.application.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ayushchavan.devboard.domain.entity.User;
import com.ayushchavan.devboard.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

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

    @Test
void createUser_shouldHashPasswordAndSaveUser() {
    String name = "Ayush Chavan";
    String email = "ayush@example.com";
    String password = "plain-password";

    when(userRepository.existsByEmail(email))
            .thenReturn(false);

    when(passwordEncoder.encode(password))
            .thenReturn("hashed-password");

    when(userRepository.save(any(User.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    User result = userService.createUser(
            name,
            email,
            password
    );

    assertNotNull(result);
    assertEquals(name, result.getName());
    assertEquals(email, result.getEmail());
    assertEquals("hashed-password", result.getPasswordHash());

    verify(userRepository).existsByEmail(email);
    verify(passwordEncoder).encode(password);
    verify(userRepository).save(any(User.class));
}

@Test
void createUser_shouldRejectDuplicateEmail() {
    String email = "ayush@example.com";

    when(userRepository.existsByEmail(email))
            .thenReturn(true);

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.createUser(
                    "Ayush Chavan",
                    email,
                    "plain-password"
            )
    );

    assertEquals("Email is already registered", exception.getMessage());

    verify(userRepository).existsByEmail(email);
    verify(passwordEncoder, never()).encode(anyString());
    verify(userRepository, never()).save(any(User.class));
}
}