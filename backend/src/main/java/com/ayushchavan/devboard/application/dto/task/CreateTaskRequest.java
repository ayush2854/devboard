package com.ayushchavan.devboard.application.dto.task;

import java.time.LocalDate;
import java.util.UUID;

import com.ayushchavan.devboard.domain.entity.TaskPriority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateTaskRequest {

@NotBlank(message = "Task title is required")
@Size(max = 200, message = "Task title must not exceed 200 characters")
private String title;

@Size(max = 2000, message = "Description must not exceed 2000 characters")
private String description;

@NotNull(message = "Task priority is required")
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

public void setTitle(String title) {
    this.title = title;
}

public void setDescription(String description) {
    this.description = description;
}

public void setPriority(TaskPriority priority) {
    this.priority = priority;
}

public void setAssigneeId(UUID assigneeId) {
    this.assigneeId = assigneeId;
}

public void setDueDate(LocalDate dueDate) {
    this.dueDate = dueDate;
}

}
