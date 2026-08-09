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

import com.ayushchavan.devboard.application.exception.AuthenticationException;
import com.ayushchavan.devboard.application.exception.ConflictException;
import com.ayushchavan.devboard.application.exception.ResourceNotFoundException;
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

        User user = createUser(
                userId,
                "Ayush Chavan",
                "ayush@example.com"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        Optional<User> result =
                userService.findById(userId);

        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
        assertEquals(
                "Ayush Chavan",
                result.get().getName()
        );

        verify(userRepository)
                .findById(userId);
    }

    @Test
    void findById_shouldReturnEmptyWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        Optional<User> result =
                userService.findById(userId);

        assertTrue(result.isEmpty());

        verify(userRepository)
                .findById(userId);
    }

    @Test
    void findByEmail_shouldReturnUserWhenUserExists() {
        String email = "ayush@example.com";
        UUID userId = UUID.randomUUID();

        User user = createUser(
                userId,
                "Ayush Chavan",
                email
        );

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        Optional<User> result =
                userService.findByEmail(email);

        assertTrue(result.isPresent());
        assertEquals(
                email,
                result.get().getEmail()
        );

        verify(userRepository)
                .findByEmail(email);
    }

    @Test
    void existsByEmail_shouldReturnTrueWhenEmailExists() {
        String email = "ayush@example.com";

        when(userRepository.existsByEmail(email))
                .thenReturn(true);

        boolean result =
                userService.existsByEmail(email);

        assertTrue(result);

        verify(userRepository)
                .existsByEmail(email);
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
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        User result = userService.createUser(
                name,
                email,
                password
        );

        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(email, result.getEmail());
        assertEquals(
                "hashed-password",
                result.getPasswordHash()
        );

        verify(userRepository)
                .existsByEmail(email);

        verify(passwordEncoder)
                .encode(password);

        verify(userRepository)
                .save(any(User.class));
    }

    @Test
    void createUser_shouldRejectDuplicateEmail() {
        String email = "ayush@example.com";

        when(userRepository.existsByEmail(email))
                .thenReturn(true);

        ConflictException exception =
                assertThrows(
                        ConflictException.class,
                        () -> userService.createUser(
                                "Ayush Chavan",
                                email,
                                "plain-password"
                        )
                );

        assertEquals(
                "Email is already registered",
                exception.getMessage()
        );

        verify(userRepository)
                .existsByEmail(email);

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void updateProfile_shouldUpdateUser() {
        UUID userId = UUID.randomUUID();

        User user = createUser(
                userId,
                "Old Name",
                "old@example.com"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByEmail(
                "new@example.com"
        )).thenReturn(false);

        when(userRepository.save(user))
                .thenReturn(user);

        User result = userService.updateProfile(
                userId,
                "New Name",
                "new@example.com"
        );

        assertEquals(
                "New Name",
                result.getName()
        );

        assertEquals(
                "new@example.com",
                result.getEmail()
        );

        verify(userRepository)
                .findById(userId);

        verify(userRepository)
                .existsByEmail("new@example.com");

        verify(userRepository)
                .save(user);
    }

    @Test
    void updateProfile_shouldRejectUnknownUser() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> userService.updateProfile(
                                userId,
                                "Ayush Chavan",
                                "ayush@example.com"
                        )
                );

        assertEquals(
                "User not found",
                exception.getMessage()
        );

        verify(userRepository)
                .findById(userId);

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void updateProfile_shouldRejectDuplicateEmail() {
        UUID userId = UUID.randomUUID();

        User user = createUser(
                userId,
                "Ayush Chavan",
                "old@example.com"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByEmail(
                "existing@example.com"
        )).thenReturn(true);

        ConflictException exception =
                assertThrows(
                        ConflictException.class,
                        () -> userService.updateProfile(
                                userId,
                                "Ayush Chavan",
                                "existing@example.com"
                        )
                );

        assertEquals(
                "Email is already registered",
                exception.getMessage()
        );

        verify(userRepository)
                .existsByEmail("existing@example.com");

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void changePassword_shouldChangePasswordWhenCurrentPasswordIsCorrect() {
        UUID userId = UUID.randomUUID();

        User user = createUser(
                userId,
                "Ayush Chavan",
                "ayush.updated@example.com"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "Password123",
                "old-hashed-password"
        )).thenReturn(true);

        when(passwordEncoder.encode(
                "NewPassword456"
        )).thenReturn("new-hashed-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        user.changePassword("old-hashed-password");

        userService.changePassword(
                userId,
                "Password123",
                "NewPassword456"
        );

        assertEquals(
                "new-hashed-password",
                user.getPasswordHash()
        );

        verify(userRepository)
                .findById(userId);

        verify(passwordEncoder)
                .matches(
                        "Password123",
                        "old-hashed-password"
                );

        verify(passwordEncoder)
                .encode("NewPassword456");

        verify(userRepository)
                .save(user);
    }

    @Test
    void changePassword_shouldRejectWhenCurrentPasswordIsIncorrect() {
        UUID userId = UUID.randomUUID();

        User user = createUser(
                userId,
                "Ayush Chavan",
                "ayush.updated@example.com"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "WrongPassword",
                "old-hashed-password"
        )).thenReturn(false);

        AuthenticationException exception =
                assertThrows(
                        AuthenticationException.class,
                        () -> userService.changePassword(
                                userId,
                                "WrongPassword",
                                "NewPassword456"
                        )
                );

        assertEquals(
                "Current password is incorrect",
                exception.getMessage()
        );

        verify(userRepository)
                .findById(userId);

        verify(passwordEncoder)
                .matches(
                        "WrongPassword",
                        "old-hashed-password"
                );

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void changePassword_shouldRejectWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> userService.changePassword(
                                userId,
                                "Password123",
                                "NewPassword456"
                        )
                );

        assertEquals(
                "User not found",
                exception.getMessage()
        );

        verify(userRepository)
                .findById(userId);

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void deleteUser_shouldDeleteUserWhenUserExists() {
        UUID userId = UUID.randomUUID();

        User user = createUser(
                userId,
                "Ayush Chavan",
                "ayush@example.com"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        verify(userRepository)
                .findById(userId);

        verify(userRepository)
                .delete(user);
    }

    @Test
    void deleteUser_shouldRejectWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> userService.deleteUser(userId)
                );

        assertEquals(
                "User not found",
                exception.getMessage()
        );

        verify(userRepository)
                .findById(userId);

        verify(userRepository, never())
                .delete(any(User.class));
    }

    @Test
    void deleteUser_shouldNotDeleteDifferentUser() {
        UUID userId = UUID.randomUUID();

        User user = createUser(
                userId,
                "Ayush Chavan",
                "ayush@example.com"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        verify(userRepository)
                .delete(user);
    }

    private User createUser(
            UUID userId,
            String name,
            String email
    ) {
        return new User(
                userId,
                name,
                email,
                "old-hashed-password",
                Instant.now(),
                Instant.now()
        );
    }
}
