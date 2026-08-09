package com.ayushchavan.devboard.application.dto.task;

import java.time.LocalDate;
import java.util.UUID;

import com.ayushchavan.devboard.domain.entity.TaskPriority;

public class CreateTaskRequest {

    private String title;
    private String description;
    private TaskPriority priority;
    private UUID assigneeId;
    private LocalDate dueDate;

    public CreateTaskRequest() {
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
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