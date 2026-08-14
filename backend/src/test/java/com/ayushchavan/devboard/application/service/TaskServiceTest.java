package com.ayushchavan.devboard.application.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ayushchavan.devboard.domain.entity.Task;
import com.ayushchavan.devboard.domain.entity.TaskPriority;
import com.ayushchavan.devboard.domain.entity.TaskStatus;
import com.ayushchavan.devboard.domain.repository.TaskRepository;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

@Mock
private TaskRepository taskRepository;

@InjectMocks
private TaskService taskService;

@Test
void findAllByProjectId_shouldReturnTasks() {

    UUID projectId = UUID.randomUUID();

    Task task1 = createTask(
            UUID.randomUUID(),
            projectId,
            "Implement login",
            "Implement JWT login",
            TaskStatus.TODO,
            TaskPriority.MEDIUM,
            UUID.randomUUID(),
            null
    );

    Task task2 = createTask(
            UUID.randomUUID(),
            projectId,
            "Implement dashboard",
            "Create dashboard APIs",
            TaskStatus.IN_PROGRESS,
            TaskPriority.HIGH,
            UUID.randomUUID(),
            UUID.randomUUID()
    );

        when(taskRepository.findAllByProjectIdAndArchivedAtIsNull(projectId))
        .thenReturn(List.of(task1, task2));

    List<Task> result =
            taskService.findAllByProjectId(projectId);

    assertEquals(2, result.size());
    assertEquals(task1, result.get(0));
    assertEquals(task2, result.get(1));

        verify(taskRepository)
        .findAllByProjectIdAndArchivedAtIsNull(projectId);
}

@Test
void findById_shouldReturnTask() {

    UUID taskId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    UUID createdBy = UUID.randomUUID();

    Task task = createTask(
            taskId,
            projectId,
            "Implement login",
            "Implement JWT login",
            TaskStatus.TODO,
            TaskPriority.MEDIUM,
            createdBy,
            null
    );

    when(taskRepository.findById(taskId))
            .thenReturn(Optional.of(task));

    Task result =
            taskService.findById(taskId);

    assertEquals(task, result);

    verify(taskRepository)
            .findById(taskId);
}

@Test
void findById_shouldThrowWhenTaskDoesNotExist() {

    UUID taskId = UUID.randomUUID();

    when(taskRepository.findById(taskId))
            .thenReturn(Optional.empty());

    IllegalArgumentException exception =
            assertThrows(
                    IllegalArgumentException.class,
                    () -> taskService.findById(taskId)
            );

    assertEquals(
            "Task not found",
            exception.getMessage()
    );

    verify(taskRepository)
            .findById(taskId);
}

@Test
void createTask_shouldCreateTaskWithTodoStatus() {

    UUID projectId = UUID.randomUUID();
    UUID createdBy = UUID.randomUUID();

    when(taskRepository.save(any(Task.class)))
            .thenAnswer(invocation ->
                    invocation.getArgument(0));

    Task result =
            taskService.createTask(
                    projectId,
                    "Implement login",
                    "Implement JWT login",
                    TaskPriority.MEDIUM,
                    createdBy,
                    null,
                    null
            );

    assertEquals(
            projectId,
            result.getProjectId()
    );

    assertEquals(
            "Implement login",
            result.getTitle()
    );

    assertEquals(
            "Implement JWT login",
            result.getDescription()
    );

    assertEquals(
            TaskStatus.TODO,
            result.getStatus()
    );

    assertEquals(
            TaskPriority.MEDIUM,
            result.getPriority()
    );

    assertEquals(
            createdBy,
            result.getCreatedBy()
    );

    assertEquals(
            null,
            result.getAssigneeId()
    );

    assertEquals(
            null,
            result.getDueDate()
    );

    verify(taskRepository)
            .save(any(Task.class));
}

@Test
void createTask_shouldCreateAssignedTask() {

    UUID projectId = UUID.randomUUID();
    UUID createdBy = UUID.randomUUID();
    UUID assigneeId = UUID.randomUUID();
    LocalDate dueDate = LocalDate.now().plusDays(7);

    when(taskRepository.save(any(Task.class)))
            .thenAnswer(invocation ->
                    invocation.getArgument(0));

    Task result =
            taskService.createTask(
                    projectId,
                    "Implement dashboard",
                    "Create dashboard APIs",
                    TaskPriority.HIGH,
                    createdBy,
                    assigneeId,
                    dueDate
            );

    assertEquals(
            projectId,
            result.getProjectId()
    );

    assertEquals(
            assigneeId,
            result.getAssigneeId()
    );

    assertEquals(
            createdBy,
            result.getCreatedBy()
    );

    assertEquals(
            TaskPriority.HIGH,
            result.getPriority()
    );

    assertEquals(
            dueDate,
            result.getDueDate()
    );

    assertEquals(
            TaskStatus.TODO,
            result.getStatus()
    );

    verify(taskRepository)
            .save(any(Task.class));
}

@Test
void updateTask_shouldUpdateTask() {

    UUID taskId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    UUID createdBy = UUID.randomUUID();
    UUID assigneeId = UUID.randomUUID();

    Instant createdAt = Instant.now();
    LocalDate dueDate = LocalDate.now().plusDays(10);

    Task existingTask =
            new Task(
                    taskId,
                    projectId,
                    "Old title",
                    "Old description",
                    TaskStatus.TODO,
                    TaskPriority.LOW,
                    createdBy,
                    null,
                    null,
                    createdAt,
                    createdAt,
                    null
            );

    when(taskRepository.findById(taskId))
            .thenReturn(Optional.of(existingTask));

    when(taskRepository.save(any(Task.class)))
            .thenAnswer(invocation ->
                    invocation.getArgument(0));

    Task result =
            taskService.updateTask(
                    taskId,
                    "Updated title",
                    "Updated description",
                    TaskStatus.IN_PROGRESS,
                    TaskPriority.HIGH,
                    assigneeId,
                    dueDate
            );

    assertEquals(
            taskId,
            result.getId()
    );

    assertEquals(
            projectId,
            result.getProjectId()
    );

    assertEquals(
            "Updated title",
            result.getTitle()
    );

    assertEquals(
            "Updated description",
            result.getDescription()
    );

    assertEquals(
            TaskStatus.IN_PROGRESS,
            result.getStatus()
    );

    assertEquals(
            TaskPriority.HIGH,
            result.getPriority()
    );

    assertEquals(
            createdBy,
            result.getCreatedBy()
    );

    assertEquals(
            assigneeId,
            result.getAssigneeId()
    );

    assertEquals(
            dueDate,
            result.getDueDate()
    );

    assertEquals(
            createdAt,
            result.getCreatedAt()
    );

    verify(taskRepository)
            .findById(taskId);

    verify(taskRepository)
            .save(any(Task.class));
}

@Test
void updateTask_shouldThrowWhenTaskDoesNotExist() {

    UUID taskId = UUID.randomUUID();

    when(taskRepository.findById(taskId))
            .thenReturn(Optional.empty());

    assertThrows(
            IllegalArgumentException.class,
            () -> taskService.updateTask(
                    taskId,
                    "Updated title",
                    "Updated description",
                    TaskStatus.DONE,
                    TaskPriority.MEDIUM,
                    null,
                    null
            )
    );

    verify(taskRepository)
            .findById(taskId);

    verify(taskRepository, never())
            .save(any(Task.class));
}

@Test
void archiveTask_shouldArchiveTask() {

    UUID taskId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    UUID createdBy = UUID.randomUUID();

    Instant createdAt = Instant.now();

    Task existingTask = new Task(
            taskId,
            projectId,
            "Implement login",
            "Implement JWT login",
            TaskStatus.IN_PROGRESS,
            TaskPriority.HIGH,
            createdBy,
            null,
            null,
            createdAt,
            createdAt,
            null
    );

    when(taskRepository.findById(taskId))
            .thenReturn(Optional.of(existingTask));

    when(taskRepository.save(any(Task.class)))
            .thenAnswer(invocation ->
                    invocation.getArgument(0));

    Task result =
            taskService.archiveTask(taskId);

    assertEquals(
            taskId,
            result.getId()
    );

    assertEquals(
            projectId,
            result.getProjectId()
    );

    assertEquals(
            "Implement login",
            result.getTitle()
    );

    assertEquals(
            TaskStatus.IN_PROGRESS,
            result.getStatus()
    );

    assertEquals(
            TaskPriority.HIGH,
            result.getPriority()
    );

    assertEquals(
            createdBy,
            result.getCreatedBy()
    );

    assertEquals(
            createdAt,
            result.getCreatedAt()
    );

    assertEquals(
            null,
            result.getAssigneeId()
    );

    assertEquals(
            null,
            result.getDueDate()
    );

    // archivedAt must be set
    org.junit.jupiter.api.Assertions.assertNotNull(
            result.getArchivedAt()
    );

    verify(taskRepository)
            .findById(taskId);

    verify(taskRepository)
            .save(any(Task.class));
}

@Test
void archiveTask_shouldThrowWhenTaskDoesNotExist() {

    UUID taskId = UUID.randomUUID();

    when(taskRepository.findById(taskId))
            .thenReturn(Optional.empty());

    assertThrows(
            IllegalArgumentException.class,
            () -> taskService.archiveTask(taskId)
    );

    verify(taskRepository)
            .findById(taskId);

    verify(taskRepository, never())
            .save(any(Task.class));
}

@Test
void archiveTask_shouldThrowWhenTaskIsAlreadyArchived() {

    UUID taskId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    UUID createdBy = UUID.randomUUID();

    Instant now = Instant.now();

    Task archivedTask = new Task(
            taskId,
            projectId,
            "Already archived",
            "Archived task",
            TaskStatus.DONE,
            TaskPriority.MEDIUM,
            createdBy,
            null,
            null,
            now,
            now,
            now
    );

    when(taskRepository.findById(taskId))
            .thenReturn(Optional.of(archivedTask));

    IllegalStateException exception =
            assertThrows(
                    IllegalStateException.class,
                    () -> taskService.archiveTask(taskId)
            );

    assertEquals(
            "Task is already archived",
            exception.getMessage()
    );

    verify(taskRepository)
            .findById(taskId);

    verify(taskRepository, never())
            .save(any(Task.class));
}

@Test
void deleteTask_shouldDeleteTask() {

    UUID taskId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    UUID createdBy = UUID.randomUUID();

    Task task = createTask(
            taskId,
            projectId,
            "Delete me",
            "Task to delete",
            TaskStatus.TODO,
            TaskPriority.MEDIUM,
            createdBy,
            null
    );

    when(taskRepository.findById(taskId))
            .thenReturn(Optional.of(task));

    taskService.deleteTask(taskId);

    verify(taskRepository)
            .findById(taskId);

    verify(taskRepository)
            .delete(task);
}

@Test
void deleteTask_shouldThrowWhenTaskDoesNotExist() {

    UUID taskId = UUID.randomUUID();

    when(taskRepository.findById(taskId))
            .thenReturn(Optional.empty());

    assertThrows(
            IllegalArgumentException.class,
            () -> taskService.deleteTask(taskId)
    );

    verify(taskRepository)
            .findById(taskId);

    verify(taskRepository, never())
            .delete(any(Task.class));
}

private Task createTask(
        UUID taskId,
        UUID projectId,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        UUID createdBy,
        UUID assigneeId
) {
    Instant now = Instant.now();

    return new Task(
            taskId,
            projectId,
            title,
            description,
            status,
            priority,
            createdBy,
            assigneeId,
            null,
            now,
            now,
            null
    );
}

}
