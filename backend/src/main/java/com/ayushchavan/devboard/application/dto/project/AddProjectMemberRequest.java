package com.ayushchavan.devboard.application.dto.project;

import java.util.UUID;

public class AddProjectMemberRequest {

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