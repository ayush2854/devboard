package com.ayushchavan.devboard.application.dto.project;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public class AddProjectMemberRequest {

@NotNull(message = "User ID is required")
private UUID userId;

public AddProjectMemberRequest() {
}

public UUID getUserId() {
    return userId;
}

public void setUserId(UUID userId) {
    this.userId = userId;
}

}
