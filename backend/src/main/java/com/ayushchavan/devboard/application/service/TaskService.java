package com.ayushchavan.devboard.application.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ayushchavan.devboard.domain.entity.Task;
import com.ayushchavan.devboard.domain.entity.TaskPriority;
import com.ayushchavan.devboard.domain.entity.TaskStatus;
import com.ayushchavan.devboard.domain.repository.TaskRepository;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(
            TaskRepository taskRepository
    ) {
        this.taskRepository = taskRepository;
    }

    public List<Task> findAllByProjectId(
        UUID projectId
    ) {
    return taskRepository
            .findAllByProjectIdAndArchivedAtIsNull(projectId);
    }

    public Task findById(
            UUID taskId
    ) {
        return taskRepository
                .findById(taskId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Task not found"
                        )
                );
    }

    public Task createTask(
            UUID projectId,
            String title,
            String description,
            TaskPriority priority,
            UUID createdBy,
            UUID assigneeId,
            LocalDate dueDate
    ) {
        Instant now = Instant.now();

        Task task = new Task(
                UUID.randomUUID(),
                projectId,
                title,
                description,
                TaskStatus.TODO,
                priority,
                createdBy,
                assigneeId,
                dueDate,
                now,
                now,
                null
        );

        return taskRepository.save(task);
    }

    public Task updateTask(
            UUID taskId,
            String title,
            String description,
            TaskStatus status,
            TaskPriority priority,
            UUID assigneeId,
            LocalDate dueDate
    ) {
        Task existingTask = findById(taskId);

        if (existingTask.getArchivedAt() != null) {
            throw new IllegalStateException(
                    "Archived task cannot be updated"
            );
        }

        Task updatedTask = new Task(
                existingTask.getId(),
                existingTask.getProjectId(),
                title,
                description,
                status,
                priority,
                existingTask.getCreatedBy(),
                assigneeId,
                dueDate,
                existingTask.getCreatedAt(),
                Instant.now(),
                existingTask.getArchivedAt()
        );

        return taskRepository.save(updatedTask);
    }

    public Task archiveTask(
            UUID taskId
    ) {
        Task existingTask = findById(taskId);

        if (existingTask.getArchivedAt() != null) {
            throw new IllegalStateException(
                    "Task is already archived"
            );
        }

        Instant now = Instant.now();

        Task archivedTask = new Task(
                existingTask.getId(),
                existingTask.getProjectId(),
                existingTask.getTitle(),
                existingTask.getDescription(),
                existingTask.getStatus(),
                existingTask.getPriority(),
                existingTask.getCreatedBy(),
                existingTask.getAssigneeId(),
                existingTask.getDueDate(),
                existingTask.getCreatedAt(),
                now,
                now
        );

        return taskRepository.save(archivedTask);
    }

    public void deleteTask(
            UUID taskId
    ) {
        Task task = findById(taskId);

        taskRepository.delete(task);
    }
}
