package com.ayushchavan.devboard.application.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ayushchavan.devboard.domain.entity.User;
import com.ayushchavan.devboard.domain.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }


    public User updateProfile(
        UUID userId,
        String name,
        String email
    ) {
    User user = userRepository.findById(userId)
            .orElseThrow(() ->
                    new IllegalArgumentException("User not found")
            );

    if (!user.getEmail().equals(email)
            && userRepository.existsByEmail(email)) {
        throw new IllegalArgumentException(
                "Email is already registered"
        );
    }

    user.updateProfile(name, email);

    return userRepository.save(user);
    }

    public User createUser(
            String name,
            String email,
            String password
    ) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        Instant now = Instant.now();

        User user = new User(
                UUID.randomUUID(),
                name,
                email,
                passwordEncoder.encode(password),
                now,
                now
        );

        return userRepository.save(user);
    }
}