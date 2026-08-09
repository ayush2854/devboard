package com.ayushchavan.devboard.application.dto.task;

import java.time.LocalDate;
import java.util.UUID;

import com.ayushchavan.devboard.domain.entity.TaskPriority;
import com.ayushchavan.devboard.domain.entity.TaskStatus;

public class UpdateTaskRequest {

    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private UUID assigneeId;
    private LocalDate dueDate;

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

    public TaskPriority getPriority() {
        return priority;
    }

    public UUID getAssigneeId() {
        return assigneeId;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }
}