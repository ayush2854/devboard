package com.ayushchavan.devboard.presentation.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ayushchavan.devboard.application.dto.task.CreateTaskRequest;
import com.ayushchavan.devboard.application.dto.task.TaskResponse;
import com.ayushchavan.devboard.application.dto.task.UpdateTaskRequest;
import com.ayushchavan.devboard.application.exception.ForbiddenException;
import com.ayushchavan.devboard.application.service.ProjectService;
import com.ayushchavan.devboard.application.service.TaskService;
import com.ayushchavan.devboard.application.service.WorkspaceMembershipService;
import com.ayushchavan.devboard.domain.entity.Project;
import com.ayushchavan.devboard.domain.entity.Task;
import com.ayushchavan.devboard.domain.entity.WorkspaceMembership;
import com.ayushchavan.devboard.domain.entity.WorkspaceRole;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/projects/{projectId}/tasks")
public class TaskController {

    private final TaskService taskService;
    private final ProjectService projectService;
    private final WorkspaceMembershipService membershipService;

    public TaskController(
            TaskService taskService,
            ProjectService projectService,
            WorkspaceMembershipService membershipService
    ) {
        this.taskService = taskService;
        this.projectService = projectService;
        this.membershipService = membershipService;
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasks(
            Authentication authentication,
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId
    ) {
        UUID authenticatedUserId =
                getAuthenticatedUserId(authentication);

        requireWorkspaceMember(
                workspaceId,
                authenticatedUserId
        );

        requireProjectBelongsToWorkspace(
                projectId,
                workspaceId
        );

        List<TaskResponse> response =
                taskService.findAllByProjectId(projectId)
                        .stream()
                        .map(TaskResponse::from)
                        .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTask(
            Authentication authentication,
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId
    ) {
        UUID authenticatedUserId =
                getAuthenticatedUserId(authentication);

        requireWorkspaceMember(
                workspaceId,
                authenticatedUserId
        );

        requireProjectBelongsToWorkspace(
                projectId,
                workspaceId
        );

        Task task =
                taskService.findById(taskId);

        requireTaskBelongsToProject(
                task,
                projectId
        );

        return ResponseEntity.ok(
                TaskResponse.from(task)
        );
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            Authentication authentication,
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @RequestBody CreateTaskRequest request
    ) {
        UUID authenticatedUserId =
                getAuthenticatedUserId(authentication);

        WorkspaceRole actorRole =
                getRequiredWorkspaceRole(
                        workspaceId,
                        authenticatedUserId
                );

        requireAdminOrOwner(actorRole);

        requireProjectBelongsToWorkspace(
                projectId,
                workspaceId
        );

        Task task =
                taskService.createTask(
                        projectId,
                        request.getTitle(),
                        request.getDescription(),
                        request.getPriority(),
                        authenticatedUserId,
                        request.getAssigneeId(),
                        request.getDueDate()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(TaskResponse.from(task));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(
            Authentication authentication,
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @RequestBody UpdateTaskRequest request
    ) {
        UUID authenticatedUserId =
                getAuthenticatedUserId(authentication);

        WorkspaceRole actorRole =
                getRequiredWorkspaceRole(
                        workspaceId,
                        authenticatedUserId
                );

        requireAdminOrOwner(actorRole);

        requireProjectBelongsToWorkspace(
                projectId,
                workspaceId
        );

        Task existingTask =
                taskService.findById(taskId);

        requireTaskBelongsToProject(
                existingTask,
                projectId
        );

        Task task =
                taskService.updateTask(
                        taskId,
                        request.getTitle(),
                        request.getDescription(),
                        request.getStatus(),
                        request.getPriority(),
                        request.getAssigneeId(),
                        request.getDueDate()
                );

        return ResponseEntity.ok(
                TaskResponse.from(task)
        );
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            Authentication authentication,
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId
    ) {
        UUID authenticatedUserId =
                getAuthenticatedUserId(authentication);

        WorkspaceRole actorRole =
                getRequiredWorkspaceRole(
                        workspaceId,
                        authenticatedUserId
                );

        requireAdminOrOwner(actorRole);

        requireProjectBelongsToWorkspace(
                projectId,
                workspaceId
        );

        Task existingTask =
                taskService.findById(taskId);

        requireTaskBelongsToProject(
                existingTask,
                projectId
        );

        taskService.deleteTask(taskId);

        return ResponseEntity.noContent().build();
    }

    private UUID getAuthenticatedUserId(
            Authentication authentication
    ) {
        Object principal =
                authentication.getPrincipal();

        if (principal instanceof UUID userId) {
            return userId;
        }

        if (principal instanceof UserDetails userDetails) {
            return UUID.fromString(
                    userDetails.getUsername()
            );
        }

        return UUID.fromString(
                authentication.getName()
        );
    }

    private WorkspaceRole getRequiredWorkspaceRole(
            UUID workspaceId,
            UUID userId
    ) {
        return membershipService
                .findByWorkspaceAndUser(
                        workspaceId,
                        userId
                )
                .map(WorkspaceMembership::getRole)
                .orElseThrow(() ->
                        new ForbiddenException(
                                "User is not a member of this workspace"
                        )
                );
    }

    private void requireWorkspaceMember(
            UUID workspaceId,
            UUID userId
    ) {
        getRequiredWorkspaceRole(
                workspaceId,
                userId
        );
    }

    private void requireAdminOrOwner(
            WorkspaceRole role
    ) {
        if (role != WorkspaceRole.OWNER
                && role != WorkspaceRole.ADMIN) {

            throw new ForbiddenException(
                    "Insufficient workspace permissions"
            );
        }
    }

    private Project requireProjectBelongsToWorkspace(
            UUID projectId,
            UUID workspaceId
    ) {
        Project project =
                projectService.findById(projectId);

        if (!project.getWorkspaceId()
                .equals(workspaceId)) {

            throw new ForbiddenException(
                    "Project does not belong to this workspace"
            );
        }

        return project;
    }

    private void requireTaskBelongsToProject(
            Task task,
            UUID projectId
    ) {
        if (!task.getProjectId()
                .equals(projectId)) {

            throw new ForbiddenException(
                    "Task does not belong to this project"
            );
        }
    }
}
