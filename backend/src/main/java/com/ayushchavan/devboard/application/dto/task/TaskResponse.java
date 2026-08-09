package com.ayushchavan.devboard.application.dto.task;

import java.time.Instant;
import java.util.UUID;

import com.ayushchavan.devboard.domain.entity.Task;
import com.ayushchavan.devboard.domain.entity.TaskStatus;

public class TaskResponse {

    private final UUID id;
    private final UUID projectId;
    private final String title;
    private final String description;
    private final TaskStatus status;
    private final UUID assigneeId;
    private final Instant createdAt;
    private final Instant updatedAt;

    private TaskResponse(
            UUID id,
            UUID projectId,
            String title,
            String description,
            TaskStatus status,
            UUID assigneeId,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.projectId = projectId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.assigneeId = assigneeId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getProjectId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getAssigneeId(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getProjectId() {
        return projectId;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}