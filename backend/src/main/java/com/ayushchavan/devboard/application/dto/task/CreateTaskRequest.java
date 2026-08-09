package com.ayushchavan.devboard.application.dto.task;

import java.util.UUID;

public class CreateTaskRequest {

    private String title;
    private String description;
    private UUID assigneeId;

    public CreateTaskRequest() {
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public UUID getAssigneeId() {
        return assigneeId;
    }
}