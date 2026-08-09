package com.ayushchavan.devboard.application.dto.workspace;

import java.util.UUID;

public class AddWorkspaceMemberRequest {

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