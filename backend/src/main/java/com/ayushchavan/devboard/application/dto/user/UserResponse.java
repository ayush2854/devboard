package com.ayushchavan.devboard.application.dto.user;

import java.time.Instant;
import java.util.UUID;

import com.ayushchavan.devboard.domain.entity.User;

public class UserResponse {

    private UUID id;
    private String name;
    private String email;
    private Instant createdAt;
    private Instant updatedAt;

    public UserResponse(
            UUID id,
            String name,
            String email,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}