package com.ayushchavan.devboard.application.dto.task;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.ayushchavan.devboard.domain.entity.Task;
import com.ayushchavan.devboard.domain.entity.TaskPriority;
import com.ayushchavan.devboard.domain.entity.TaskStatus;

public class TaskResponse {

    private final UUID id;
    private final UUID projectId;
    private final String title;
    private final String description;
    private final TaskStatus status;
    private final TaskPriority priority;
    private final UUID createdBy;
    private final UUID assigneeId;
    private final LocalDate dueDate;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Instant archivedAt;

    private TaskResponse(
            UUID id,
            UUID projectId,
            String title,
            String description,
            TaskStatus status,
            TaskPriority priority,
            UUID createdBy,
            UUID assigneeId,
            LocalDate dueDate,
            Instant createdAt,
            Instant updatedAt,
            Instant archivedAt
    ) {
        this.id = id;
        this.projectId = projectId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.createdBy = createdBy;
        this.assigneeId = assigneeId;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.archivedAt = archivedAt;
    }

    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getProjectId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getCreatedBy(),
                task.getAssigneeId(),
                task.getDueDate(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getArchivedAt()
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

    public TaskPriority getPriority() {
        return priority;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public UUID getAssigneeId() {
        return assigneeId;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getArchivedAt() {
        return archivedAt;
    }
}