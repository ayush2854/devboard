package com.ayushchavan.devboard.application.dto.task;

import java.util.UUID;

import com.ayushchavan.devboard.domain.entity.TaskStatus;

public class UpdateTaskRequest {

    private String title;
    private String description;
    private TaskStatus status;
    private UUID assigneeId;

    public UpdateTaskRequest() {
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public UUID getAssigneeId() {
        return assigneeId;
    }
}