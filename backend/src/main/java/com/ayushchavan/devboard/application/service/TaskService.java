package com.ayushchavan.devboard.application.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ayushchavan.devboard.domain.entity.Task;
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
                .findAllByProjectId(projectId);
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
            UUID assigneeId
    ) {
        Instant now = Instant.now();

        Task task = new Task(
                UUID.randomUUID(),
                projectId,
                title,
                description,
                TaskStatus.TODO,
                assigneeId,
                now,
                now
        );

        return taskRepository.save(task);
    }

    public Task updateTask(
            UUID taskId,
            String title,
            String description,
            TaskStatus status,
            UUID assigneeId
    ) {
        Task existingTask = findById(taskId);

        Task updatedTask = new Task(
                existingTask.getId(),
                existingTask.getProjectId(),
                title,
                description,
                status,
                assigneeId,
                existingTask.getCreatedAt(),
                Instant.now()
        );

        return taskRepository.save(updatedTask);
    }

    public void deleteTask(
            UUID taskId
    ) {
        Task task = findById(taskId);

        taskRepository.delete(task);
    }
}
