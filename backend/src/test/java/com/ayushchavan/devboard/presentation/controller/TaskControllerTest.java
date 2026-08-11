package com.ayushchavan.devboard.presentation.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ayushchavan.devboard.application.service.JwtService;
import com.ayushchavan.devboard.application.service.TaskService;
import com.ayushchavan.devboard.application.service.WorkspaceMembershipService;
import com.ayushchavan.devboard.domain.entity.Project;
import com.ayushchavan.devboard.domain.entity.ProjectStatus;
import com.ayushchavan.devboard.domain.entity.Task;
import com.ayushchavan.devboard.domain.entity.TaskPriority;
import com.ayushchavan.devboard.domain.entity.TaskStatus;
import com.ayushchavan.devboard.domain.entity.WorkspaceMembership;
import com.ayushchavan.devboard.domain.entity.WorkspaceRole;
import com.ayushchavan.devboard.domain.repository.ProjectRepository;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private WorkspaceMembershipService membershipService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private ProjectRepository projectRepository;

    @Test
    void getTasks_shouldReturnTasksForWorkspaceMember()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Project project = createProject(
                projectId,
                workspaceId
        );

        Task firstTask = createTask(
                UUID.randomUUID(),
                projectId,
                "Implement authentication",
                "Implement JWT authentication",
                TaskStatus.TODO,
                TaskPriority.MEDIUM,
                userId,
                null
        );

        Task secondTask = createTask(
                UUID.randomUUID(),
                projectId,
                "Create dashboard",
                "Create dashboard UI",
                TaskStatus.IN_PROGRESS,
                TaskPriority.HIGH,
                userId,
                userId
        );

        WorkspaceMembership membership =
                createMembership(
                        workspaceId,
                        userId,
                        WorkspaceRole.MEMBER
                );

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                userId
        )).thenReturn(Optional.of(membership));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(taskService.findAllByProjectId(projectId))
                .thenReturn(List.of(firstTask, secondTask));

        mockMvc.perform(
                get(
                        "/api/workspaces/{workspaceId}/projects/{projectId}/tasks",
                        workspaceId,
                        projectId
                )
                .with(user(userId.toString()))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()", is(2)))
        .andExpect(jsonPath(
                "$[0].id",
                is(firstTask.getId().toString())
        ))
        .andExpect(jsonPath(
                "$[0].title",
                is("Implement authentication")
        ))
        .andExpect(jsonPath(
                "$[0].status",
                is("TODO")
        ))
        .andExpect(jsonPath(
                "$[0].priority",
                is("MEDIUM")
        ))
        .andExpect(jsonPath(
                "$[1].id",
                is(secondTask.getId().toString())
        ))
        .andExpect(jsonPath(
                "$[1].title",
                is("Create dashboard")
        ))
        .andExpect(jsonPath(
                "$[1].status",
                is("IN_PROGRESS")
        ))
        .andExpect(jsonPath(
                "$[1].priority",
                is("HIGH")
        ));
    }

    @Test
    void getTasks_shouldRejectNonWorkspaceMember()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                userId
        )).thenReturn(Optional.empty());

        mockMvc.perform(
                get(
                        "/api/workspaces/{workspaceId}/projects/{projectId}/tasks",
                        workspaceId,
                        projectId
                )
                .with(user(userId.toString()))
        )
        .andExpect(status().isForbidden());

        verify(projectRepository, never())
                .findById(projectId);

        verify(taskService, never())
                .findAllByProjectId(projectId);
    }

    @Test
    void getTasks_shouldRejectProjectFromAnotherWorkspace()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID anotherWorkspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Project project = createProject(
                projectId,
                anotherWorkspaceId
        );

        WorkspaceMembership membership =
                createMembership(
                        workspaceId,
                        userId,
                        WorkspaceRole.MEMBER
                );

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                userId
        )).thenReturn(Optional.of(membership));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        mockMvc.perform(
                get(
                        "/api/workspaces/{workspaceId}/projects/{projectId}/tasks",
                        workspaceId,
                        projectId
                )
                .with(user(userId.toString()))
        )
        .andExpect(status().isForbidden());

        verify(taskService, never())
                .findAllByProjectId(projectId);
    }

    @Test
    void getTask_shouldReturnTaskForWorkspaceMember()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Project project = createProject(
                projectId,
                workspaceId
        );

        Task task = createTask(
                taskId,
                projectId,
                "Implement authentication",
                "Implement JWT authentication",
                TaskStatus.IN_PROGRESS,
                TaskPriority.HIGH,
                userId,
                userId
        );

        WorkspaceMembership membership =
                createMembership(
                        workspaceId,
                        userId,
                        WorkspaceRole.MEMBER
                );

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                userId
        )).thenReturn(Optional.of(membership));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(taskService.findById(taskId))
                .thenReturn(task);

        mockMvc.perform(
                get(
                        "/api/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}",
                        workspaceId,
                        projectId,
                        taskId
                )
                .with(user(userId.toString()))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath(
                "$.id",
                is(taskId.toString())
        ))
        .andExpect(jsonPath(
                "$.projectId",
                is(projectId.toString())
        ))
        .andExpect(jsonPath(
                "$.title",
                is("Implement authentication")
        ))
        .andExpect(jsonPath(
                "$.description",
                is("Implement JWT authentication")
        ))
        .andExpect(jsonPath(
                "$.status",
                is("IN_PROGRESS")
        ))
        .andExpect(jsonPath(
                "$.priority",
                is("HIGH")
        ))
        .andExpect(jsonPath(
                "$.assigneeId",
                is(userId.toString())
        ));
    }

    @Test
    void getTask_shouldRejectTaskFromAnotherProject()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID anotherProjectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Project project = createProject(
                projectId,
                workspaceId
        );

        Task task = createTask(
                taskId,
                anotherProjectId,
                "Other task",
                "Other project task",
                TaskStatus.TODO,
                TaskPriority.MEDIUM,
                userId,
                null
        );

        WorkspaceMembership membership =
                createMembership(
                        workspaceId,
                        userId,
                        WorkspaceRole.MEMBER
                );

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                userId
        )).thenReturn(Optional.of(membership));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(taskService.findById(taskId))
                .thenReturn(task);

        mockMvc.perform(
                get(
                        "/api/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}",
                        workspaceId,
                        projectId,
                        taskId
                )
                .with(user(userId.toString()))
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void createTask_shouldReturnCreatedTaskForAdmin()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        Project project = createProject(
                projectId,
                workspaceId
        );

        Task task = createTask(
                taskId,
                projectId,
                "Implement authentication",
                "Implement JWT authentication",
                TaskStatus.TODO,
                TaskPriority.MEDIUM,
                adminId,
                adminId
        );

        WorkspaceMembership membership =
                createMembership(
                        workspaceId,
                        adminId,
                        WorkspaceRole.ADMIN
                );

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                adminId
        )).thenReturn(Optional.of(membership));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(taskService.createTask(
                projectId,
                "Implement authentication",
                "Implement JWT authentication",
                TaskPriority.MEDIUM,
                adminId,
                adminId,
                null
        )).thenReturn(task);

        mockMvc.perform(
                post(
                        "/api/workspaces/{workspaceId}/projects/{projectId}/tasks",
                        workspaceId,
                        projectId
                )
                .with(user(adminId.toString()))
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                            "title": "Implement authentication",
                            "description": "Implement JWT authentication",
                            "priority": "MEDIUM",
                            "assigneeId": "%s"
                        }
                        """.formatted(adminId))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath(
                "$.id",
                is(taskId.toString())
        ))
        .andExpect(jsonPath(
                "$.projectId",
                is(projectId.toString())
        ))
        .andExpect(jsonPath(
                "$.title",
                is("Implement authentication")
        ))
        .andExpect(jsonPath(
                "$.status",
                is("TODO")
        ))
        .andExpect(jsonPath(
                "$.priority",
                is("MEDIUM")
        ))
        .andExpect(jsonPath(
                "$.createdBy",
                is(adminId.toString())
        ))
        .andExpect(jsonPath(
                "$.assigneeId",
                is(adminId.toString())
        ));
    }

    @Test
    void createTask_shouldReturnCreatedTaskForOwner()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        Project project = createProject(
                projectId,
                workspaceId
        );

        Task task = createTask(
                taskId,
                projectId,
                "Create dashboard",
                "Create dashboard UI",
                TaskStatus.TODO,
                TaskPriority.MEDIUM,
                ownerId,
                null
        );

        WorkspaceMembership membership =
                createMembership(
                        workspaceId,
                        ownerId,
                        WorkspaceRole.OWNER
                );

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                ownerId
        )).thenReturn(Optional.of(membership));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(taskService.createTask(
                projectId,
                "Create dashboard",
                "Create dashboard UI",
                TaskPriority.MEDIUM,
                ownerId,
                null,
                null
        )).thenReturn(task);

        mockMvc.perform(
                post(
                        "/api/workspaces/{workspaceId}/projects/{projectId}/tasks",
                        workspaceId,
                        projectId
                )
                .with(user(ownerId.toString()))
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                            "title": "Create dashboard",
                            "description": "Create dashboard UI",
                            "priority": "MEDIUM"
                        }
                        """)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath(
                "$.id",
                is(taskId.toString())
        ))
        .andExpect(jsonPath(
                "$.projectId",
                is(projectId.toString())
        ))
        .andExpect(jsonPath(
                "$.title",
                is("Create dashboard")
        ))
        .andExpect(jsonPath(
                "$.status",
                is("TODO")
        ))
        .andExpect(jsonPath(
                "$.priority",
                is("MEDIUM")
        ))
        .andExpect(jsonPath(
                "$.createdBy",
                is(ownerId.toString())
        ));
    }

    @Test
    void createTask_shouldRejectMember()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        WorkspaceMembership membership =
                createMembership(
                        workspaceId,
                        memberId,
                        WorkspaceRole.MEMBER
                );

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                memberId
        )).thenReturn(Optional.of(membership));

        mockMvc.perform(
                post(
                        "/api/workspaces/{workspaceId}/projects/{projectId}/tasks",
                        workspaceId,
                        projectId
                )
                .with(user(memberId.toString()))
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                            "title": "Create dashboard",
                            "description": "Create dashboard UI"
                        }
                        """)
        )
        .andExpect(status().isForbidden());

        verify(projectRepository, never())
                .findById(projectId);

        verify(taskService, never())
                .createTask(
                        any(UUID.class),
                        any(String.class),
                        any(String.class),
                        any(TaskPriority.class),
                        any(UUID.class),
                        any(UUID.class),
                        any(LocalDate.class)
                );
    }

    @Test
    void createTask_shouldRejectProjectFromAnotherWorkspace()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID anotherWorkspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        Project project = createProject(
                projectId,
                anotherWorkspaceId
        );

        WorkspaceMembership membership =
                createMembership(
                        workspaceId,
                        adminId,
                        WorkspaceRole.ADMIN
                );

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                adminId
        )).thenReturn(Optional.of(membership));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        mockMvc.perform(
                post(
                        "/api/workspaces/{workspaceId}/projects/{projectId}/tasks",
                        workspaceId,
                        projectId
                )
                .with(user(adminId.toString()))
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                            "title": "Create dashboard",
                            "description": "Create dashboard UI"
                        }
                        """)
        )
        .andExpect(status().isForbidden());

        verify(taskService, never())
                .createTask(
                        any(UUID.class),
                        any(String.class),
                        any(String.class),
                        any(TaskPriority.class),
                        any(UUID.class),
                        any(UUID.class),
                        any(LocalDate.class)
                );
    }

    @Test
    void updateTask_shouldReturnUpdatedTaskForAdmin()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        Project project = createProject(
                projectId,
                workspaceId
        );

        Task existingTask = createTask(
                taskId,
                projectId,
                "Old title",
                "Old description",
                TaskStatus.TODO,
                TaskPriority.LOW,
                adminId,
                null
        );

        Task updatedTask = createTask(
                taskId,
                projectId,
                "Updated title",
                "Updated description",
                TaskStatus.DONE,
                TaskPriority.HIGH,
                adminId,
                adminId
        );

        WorkspaceMembership membership =
                createMembership(
                        workspaceId,
                        adminId,
                        WorkspaceRole.ADMIN
                );

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                adminId
        )).thenReturn(Optional.of(membership));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(taskService.findById(taskId))
                .thenReturn(existingTask);

        when(taskService.updateTask(
                taskId,
                "Updated title",
                "Updated description",
                TaskStatus.DONE,
                TaskPriority.HIGH,
                adminId,
                null
        )).thenReturn(updatedTask);

        mockMvc.perform(
                put(
                        "/api/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}",
                        workspaceId,
                        projectId,
                        taskId
                )
                .with(user(adminId.toString()))
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                            "title": "Updated title",
                            "description": "Updated description",
                            "status": "DONE",
                            "priority": "HIGH",
                            "assigneeId": "%s"
                        }
                        """.formatted(adminId))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath(
                "$.id",
                is(taskId.toString())
        ))
        .andExpect(jsonPath(
                "$.title",
                is("Updated title")
        ))
        .andExpect(jsonPath(
                "$.description",
                is("Updated description")
        ))
        .andExpect(jsonPath(
                "$.status",
                is("DONE")
        ))
        .andExpect(jsonPath(
                "$.priority",
                is("HIGH")
        ))
        .andExpect(jsonPath(
                "$.assigneeId",
                is(adminId.toString())
        ));
    }

    @Test
    void updateTask_shouldRejectMember()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        WorkspaceMembership membership =
                createMembership(
                        workspaceId,
                        memberId,
                        WorkspaceRole.MEMBER
                );

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                memberId
        )).thenReturn(Optional.of(membership));

        mockMvc.perform(
                put(
                        "/api/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}",
                        workspaceId,
                        projectId,
                        taskId
                )
                .with(user(memberId.toString()))
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                            "title": "Updated title",
                            "description": "Updated description",
                            "status": "DONE"
                        }
                        """)
        )
        .andExpect(status().isForbidden());

        verify(projectRepository, never())
                .findById(projectId);

        verify(taskService, never())
                .findById(taskId);

        verify(taskService, never())
                .updateTask(
                        any(UUID.class),
                        any(String.class),
                        any(String.class),
                        any(TaskStatus.class),
                        any(TaskPriority.class),
                        any(UUID.class),
                        any(LocalDate.class)
                );
    }

    @Test
    void updateTask_shouldRejectTaskFromAnotherProject()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID anotherProjectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        Project project = createProject(
                projectId,
                workspaceId
        );

        Task task = createTask(
                taskId,
                anotherProjectId,
                "Other task",
                "Other project task",
                TaskStatus.TODO,
                TaskPriority.MEDIUM,
                adminId,
                null
        );

        WorkspaceMembership membership =
                createMembership(
                        workspaceId,
                        adminId,
                        WorkspaceRole.ADMIN
                );

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                adminId
        )).thenReturn(Optional.of(membership));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(taskService.findById(taskId))
                .thenReturn(task);

        mockMvc.perform(
                put(
                        "/api/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}",
                        workspaceId,
                        projectId,
                        taskId
                )
                .with(user(adminId.toString()))
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                            "title": "Updated title",
                            "description": "Updated description",
                            "status": "DONE",
                            "priority": "HIGH"
                        }
                        """)
        )
        .andExpect(status().isForbidden());

        verify(taskService, never())
                .updateTask(
                        any(UUID.class),
                        any(String.class),
                        any(String.class),
                        any(TaskStatus.class),
                        any(TaskPriority.class),
                        any(UUID.class),
                        any(LocalDate.class)
                );
    }

    @Test
    void deleteTask_shouldReturnNoContentForAdmin()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        Project project = createProject(
                projectId,
                workspaceId
        );

        Task task = createTask(
                taskId,
                projectId,
                "Task",
                "Task description",
                TaskStatus.TODO,
                TaskPriority.MEDIUM,
                adminId,
                null
        );

        WorkspaceMembership membership =
                createMembership(
                        workspaceId,
                        adminId,
                        WorkspaceRole.ADMIN
                );

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                adminId
        )).thenReturn(Optional.of(membership));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(taskService.findById(taskId))
                .thenReturn(task);

        doNothing().when(taskService)
                .deleteTask(taskId);

        mockMvc.perform(
                delete(
                        "/api/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}",
                        workspaceId,
                        projectId,
                        taskId
                )
                .with(user(adminId.toString()))
                .with(csrf())
        )
        .andExpect(status().isNoContent());

        verify(taskService)
                .deleteTask(taskId);
    }

    @Test
    void deleteTask_shouldRejectMember()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        WorkspaceMembership membership =
                createMembership(
                        workspaceId,
                        memberId,
                        WorkspaceRole.MEMBER
                );

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                memberId
        )).thenReturn(Optional.of(membership));

        mockMvc.perform(
                delete(
                        "/api/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}",
                        workspaceId,
                        projectId,
                        taskId
                )
                .with(user(memberId.toString()))
                .with(csrf())
        )
        .andExpect(status().isForbidden());

        verify(projectRepository, never())
                .findById(projectId);

        verify(taskService, never())
                .findById(taskId);

        verify(taskService, never())
                .deleteTask(taskId);
    }

    @Test
    void deleteTask_shouldRejectTaskFromAnotherProject()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID anotherProjectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        Project project = createProject(
                projectId,
                workspaceId
        );

        Task task = createTask(
                taskId,
                anotherProjectId,
                "Other task",
                "Other project task",
                TaskStatus.TODO,
                TaskPriority.MEDIUM,
                adminId,
                null
        );

        WorkspaceMembership membership =
                createMembership(
                        workspaceId,
                        adminId,
                        WorkspaceRole.ADMIN
                );

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                adminId
        )).thenReturn(Optional.of(membership));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(taskService.findById(taskId))
                .thenReturn(task);

        mockMvc.perform(
                delete(
                        "/api/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}",
                        workspaceId,
                        projectId,
                        taskId
                )
                .with(user(adminId.toString()))
                .with(csrf())
        )
        .andExpect(status().isForbidden());

        verify(taskService, never())
                .deleteTask(taskId);
    }

    private Project createProject(
            UUID projectId,
            UUID workspaceId
    ) {
        Instant now = Instant.now();

        return new Project(
                projectId,
                workspaceId,
                "Backend Project",
                "Backend development project",
                ProjectStatus.ACTIVE,
                now,
                now,
                null
        );
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

    private WorkspaceMembership createMembership(
            UUID workspaceId,
            UUID userId,
            WorkspaceRole role
    ) {
        return new WorkspaceMembership(
                UUID.randomUUID(),
                workspaceId,
                userId,
                role,
                Instant.now()
        );
    }
}
