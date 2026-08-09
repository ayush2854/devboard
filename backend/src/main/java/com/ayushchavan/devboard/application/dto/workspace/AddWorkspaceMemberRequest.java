package com.ayushchavan.devboard.application.dto.workspace;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public class AddWorkspaceMemberRequest {

@NotNull(message = "User ID is required")
private UUID userId;

public AddWorkspaceMemberRequest() {
    // Required for JSON deserialization
}

public UUID getUserId() {
    return userId;
}

public void setUserId(UUID userId) {
    this.userId = userId;
}

}
