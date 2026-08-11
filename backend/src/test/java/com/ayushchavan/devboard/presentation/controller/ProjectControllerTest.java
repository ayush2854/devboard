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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ayushchavan.devboard.application.service.JwtService;
import com.ayushchavan.devboard.application.service.ProjectService;
import com.ayushchavan.devboard.application.service.WorkspaceMembershipService;
import com.ayushchavan.devboard.domain.entity.Project;
import com.ayushchavan.devboard.domain.entity.ProjectStatus;
import com.ayushchavan.devboard.domain.entity.WorkspaceMembership;
import com.ayushchavan.devboard.domain.entity.WorkspaceRole;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private WorkspaceMembershipService membershipService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void getProjects_shouldReturnProjectsForWorkspaceMember()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Project backendProject = createProject(
                UUID.randomUUID(),
                workspaceId,
                "Backend Project",
                "Backend development project",
                ProjectStatus.ACTIVE
        );

        Project frontendProject = createProject(
                UUID.randomUUID(),
                workspaceId,
                "Frontend Project",
                "Frontend development project",
                ProjectStatus.ACTIVE
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

        when(projectService.findAllByWorkspaceId(workspaceId))
                .thenReturn(List.of(
                        backendProject,
                        frontendProject
                ));

        mockMvc.perform(
                get(
                        "/api/workspaces/{workspaceId}/projects",
                        workspaceId
                )
                .with(user(userId.toString()))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()", is(2)))
        .andExpect(jsonPath(
                "$[0].id",
                is(backendProject.getId().toString())
        ))
        .andExpect(jsonPath(
                "$[0].name",
                is("Backend Project")
        ))
        .andExpect(jsonPath(
                "$[0].status",
                is("ACTIVE")
        ))
        .andExpect(jsonPath(
                "$[1].id",
                is(frontendProject.getId().toString())
        ))
        .andExpect(jsonPath(
                "$[1].name",
                is("Frontend Project")
        ));
    }

    @Test
    void getProjects_shouldRejectNonWorkspaceMember()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                userId
        )).thenReturn(Optional.empty());

        mockMvc.perform(
                get(
                        "/api/workspaces/{workspaceId}/projects",
                        workspaceId
                )
                .with(user(userId.toString()))
        )
        .andExpect(status().isForbidden());

        verify(projectService, never())
                .findAllByWorkspaceId(workspaceId);
    }

    @Test
    void getProject_shouldReturnProjectForWorkspaceMember()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Project project = createProject(
                projectId,
                workspaceId,
                "Backend Project",
                "Backend development project",
                ProjectStatus.ACTIVE
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

        when(projectService.findById(projectId))
                .thenReturn(project);

        mockMvc.perform(
                get(
                        "/api/workspaces/{workspaceId}/projects/{projectId}",
                        workspaceId,
                        projectId
                )
                .with(user(userId.toString()))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath(
                "$.id",
                is(projectId.toString())
        ))
        .andExpect(jsonPath(
                "$.workspaceId",
                is(workspaceId.toString())
        ))
        .andExpect(jsonPath(
                "$.name",
                is("Backend Project")
        ))
        .andExpect(jsonPath(
                "$.description",
                is("Backend development project")
        ))
        .andExpect(jsonPath(
                "$.status",
                is("ACTIVE")
        ));
    }

    @Test
    void getProject_shouldRejectNonWorkspaceMember()
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
                        "/api/workspaces/{workspaceId}/projects/{projectId}",
                        workspaceId,
                        projectId
                )
                .with(user(userId.toString()))
        )
        .andExpect(status().isForbidden());

        verify(projectService, never())
                .findById(projectId);
    }

    @Test
    void getProject_shouldRejectProjectFromAnotherWorkspace()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID anotherWorkspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Project project = createProject(
                projectId,
                anotherWorkspaceId,
                "Other Project",
                "Other workspace project",
                ProjectStatus.ACTIVE
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

        when(projectService.findById(projectId))
                .thenReturn(project);

        mockMvc.perform(
                get(
                        "/api/workspaces/{workspaceId}/projects/{projectId}",
                        workspaceId,
                        projectId
                )
                .with(user(userId.toString()))
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void createProject_shouldReturnCreatedProjectForAdmin()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        Project project = createProject(
                projectId,
                workspaceId,
                "Backend Project",
                "Backend development project",
                ProjectStatus.ACTIVE
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

        when(projectService.createProject(
                workspaceId,
                "Backend Project",
                "Backend development project"
        )).thenReturn(project);

        mockMvc.perform(
                post(
                        "/api/workspaces/{workspaceId}/projects",
                        workspaceId
                )
                .with(user(adminId.toString()))
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                            "name": "Backend Project",
                            "description": "Backend development project"
                        }
                        """)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath(
                "$.id",
                is(projectId.toString())
        ))
        .andExpect(jsonPath(
                "$.workspaceId",
                is(workspaceId.toString())
        ))
        .andExpect(jsonPath(
                "$.name",
                is("Backend Project")
        ))
        .andExpect(jsonPath(
                "$.status",
                is("ACTIVE")
        ));
    }

    @Test
    void createProject_shouldReturnCreatedProjectForOwner()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        Project project = createProject(
                projectId,
                workspaceId,
                "Backend Project",
                "Backend development project",
                ProjectStatus.ACTIVE
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

        when(projectService.createProject(
                workspaceId,
                "Backend Project",
                "Backend development project"
        )).thenReturn(project);

        mockMvc.perform(
                post(
                        "/api/workspaces/{workspaceId}/projects",
                        workspaceId
                )
                .with(user(ownerId.toString()))
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                            "name": "Backend Project",
                            "description": "Backend development project"
                        }
                        """)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath(
                "$.id",
                is(projectId.toString())
        ));
    }

    @Test
    void createProject_shouldRejectMember()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
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
                        "/api/workspaces/{workspaceId}/projects",
                        workspaceId
                )
                .with(user(memberId.toString()))
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                            "name": "Backend Project",
                            "description": "Backend development project"
                        }
                        """)
        )
        .andExpect(status().isForbidden());

        verify(projectService, never())
                .createProject(
                        any(UUID.class),
                        any(String.class),
                        any(String.class)
                );
    }

    @Test
    void updateProject_shouldReturnUpdatedProjectForAdmin()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        Project project = createProject(
                projectId,
                workspaceId,
                "Updated Project",
                "Updated description",
                ProjectStatus.ACTIVE
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

        when(projectService.findById(projectId))
                .thenReturn(project);

        when(projectService.updateProject(
                projectId,
                "Updated Project",
                "Updated description"
        )).thenReturn(project);

        mockMvc.perform(
                put(
                        "/api/workspaces/{workspaceId}/projects/{projectId}",
                        workspaceId,
                        projectId
                )
                .with(user(adminId.toString()))
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                            "name": "Updated Project",
                            "description": "Updated description"
                        }
                        """)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath(
                "$.id",
                is(projectId.toString())
        ))
        .andExpect(jsonPath(
                "$.name",
                is("Updated Project")
        ))
        .andExpect(jsonPath(
                "$.description",
                is("Updated description")
        ));
    }

    @Test
    void updateProject_shouldRejectMember()
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
                put(
                        "/api/workspaces/{workspaceId}/projects/{projectId}",
                        workspaceId,
                        projectId
                )
                .with(user(memberId.toString()))
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                            "name": "Updated Project",
                            "description": "Updated description"
                        }
                        """)
        )
        .andExpect(status().isForbidden());

        verify(projectService, never())
                .updateProject(
                        any(UUID.class),
                        any(String.class),
                        any(String.class)
                );
    }

    @Test
    void updateProject_shouldRejectProjectFromAnotherWorkspace()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID anotherWorkspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        Project project = createProject(
                projectId,
                anotherWorkspaceId,
                "Other Project",
                "Other description",
                ProjectStatus.ACTIVE
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

        when(projectService.findById(projectId))
                .thenReturn(project);

        mockMvc.perform(
                put(
                        "/api/workspaces/{workspaceId}/projects/{projectId}",
                        workspaceId,
                        projectId
                )
                .with(user(adminId.toString()))
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                            "name": "Updated Project",
                            "description": "Updated description"
                        }
                        """)
        )
        .andExpect(status().isForbidden());

        verify(projectService, never())
                .updateProject(
                        any(UUID.class),
                        any(String.class),
                        any(String.class)
                );
    }

    @Test
    void archiveProject_shouldReturnArchivedProjectForAdmin()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        Project project = createProject(
                projectId,
                workspaceId,
                "Backend Project",
                "Backend project",
                ProjectStatus.ARCHIVED
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

        when(projectService.findById(projectId))
                .thenReturn(project);

        when(projectService.archiveProject(projectId))
                .thenReturn(project);

        mockMvc.perform(
                put(
                        "/api/workspaces/{workspaceId}/projects/{projectId}/archive",
                        workspaceId,
                        projectId
                )
                .with(user(adminId.toString()))
                .with(csrf())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath(
                "$.id",
                is(projectId.toString())
        ))
        .andExpect(jsonPath(
                "$.status",
                is("ARCHIVED")
        ));
    }

    @Test
    void archiveProject_shouldRejectMember()
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
                put(
                        "/api/workspaces/{workspaceId}/projects/{projectId}/archive",
                        workspaceId,
                        projectId
                )
                .with(user(memberId.toString()))
                .with(csrf())
        )
        .andExpect(status().isForbidden());

        verify(projectService, never())
                .archiveProject(projectId);
    }

    @Test
    void archiveProject_shouldRejectProjectFromAnotherWorkspace()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID anotherWorkspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        Project project = createProject(
                projectId,
                anotherWorkspaceId,
                "Other Project",
                "Other description",
                ProjectStatus.ACTIVE
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

        when(projectService.findById(projectId))
                .thenReturn(project);

        mockMvc.perform(
                put(
                        "/api/workspaces/{workspaceId}/projects/{projectId}/archive",
                        workspaceId,
                        projectId
                )
                .with(user(adminId.toString()))
                .with(csrf())
        )
        .andExpect(status().isForbidden());

        verify(projectService, never())
                .archiveProject(projectId);
    }

    @Test
    void deleteProject_shouldReturnNoContentForAdmin()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        Project project = createProject(
                projectId,
                workspaceId,
                "Backend Project",
                "Backend project",
                ProjectStatus.ACTIVE
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

        when(projectService.findById(projectId))
                .thenReturn(project);

        doNothing().when(projectService)
                .deleteProject(projectId);

        mockMvc.perform(
                delete(
                        "/api/workspaces/{workspaceId}/projects/{projectId}",
                        workspaceId,
                        projectId
                )
                .with(user(adminId.toString()))
                .with(csrf())
        )
        .andExpect(status().isNoContent());

        verify(projectService)
                .deleteProject(projectId);
    }

    @Test
    void deleteProject_shouldRejectMember()
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
                delete(
                        "/api/workspaces/{workspaceId}/projects/{projectId}",
                        workspaceId,
                        projectId
                )
                .with(user(memberId.toString()))
                .with(csrf())
        )
        .andExpect(status().isForbidden());

        verify(projectService, never())
                .deleteProject(projectId);
    }

    @Test
    void deleteProject_shouldRejectProjectFromAnotherWorkspace()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID anotherWorkspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        Project project = createProject(
                projectId,
                anotherWorkspaceId,
                "Other Project",
                "Other description",
                ProjectStatus.ACTIVE
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

        when(projectService.findById(projectId))
                .thenReturn(project);

        mockMvc.perform(
                delete(
                        "/api/workspaces/{workspaceId}/projects/{projectId}",
                        workspaceId,
                        projectId
                )
                .with(user(adminId.toString()))
                .with(csrf())
        )
        .andExpect(status().isForbidden());

        verify(projectService, never())
                .deleteProject(projectId);
    }

    @Test
void createProject_shouldRejectBlankName()
        throws Exception {

    UUID workspaceId = UUID.randomUUID();
    UUID adminId = UUID.randomUUID();

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

    mockMvc.perform(
            post(
                    "/api/workspaces/{workspaceId}/projects",
                    workspaceId
            )
            .with(user(adminId.toString()))
            .with(csrf())
            .contentType("application/json")
            .content("""
                    {
                        "name": "",
                        "description": "Backend project"
                    }
                    """)
    )
    .andExpect(status().isBadRequest())
    .andExpect(jsonPath(
            "$.status",
            is(400)
    ))
    .andExpect(jsonPath(
            "$.message",
            is("Project name is required")
    ));

    verify(projectService, never())
            .createProject(
                    any(UUID.class),
                    any(String.class),
                    any(String.class)
            );
}

@Test
void createProject_shouldRejectNameExceedingMaximumLength()
        throws Exception {

    UUID workspaceId = UUID.randomUUID();
    UUID adminId = UUID.randomUUID();

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

    String longName = "A".repeat(101);

    mockMvc.perform(
            post(
                    "/api/workspaces/{workspaceId}/projects",
                    workspaceId
            )
            .with(user(adminId.toString()))
            .with(csrf())
            .contentType("application/json")
            .content("""
                    {
                        "name": "%s",
                        "description": "Backend project"
                    }
                    """.formatted(longName))
    )
    .andExpect(status().isBadRequest())
    .andExpect(jsonPath(
            "$.status",
            is(400)
    ))
    .andExpect(jsonPath(
            "$.message",
            is("Project name must not exceed 100 characters")
    ));

    verify(projectService, never())
            .createProject(
                    any(UUID.class),
                    any(String.class),
                    any(String.class)
            );
}

@Test
void updateProject_shouldRejectBlankName()
        throws Exception {

    UUID workspaceId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    UUID adminId = UUID.randomUUID();

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

    mockMvc.perform(
            put(
                    "/api/workspaces/{workspaceId}/projects/{projectId}",
                    workspaceId,
                    projectId
            )
            .with(user(adminId.toString()))
            .with(csrf())
            .contentType("application/json")
            .content("""
                    {
                        "name": "",
                        "description": "Updated project"
                    }
                    """)
    )
    .andExpect(status().isBadRequest())
    .andExpect(jsonPath(
            "$.status",
            is(400)
    ))
    .andExpect(jsonPath(
            "$.message",
            is("Project name is required")
    ));

    verify(projectService, never())
            .findById(projectId);

    verify(projectService, never())
            .updateProject(
                    any(UUID.class),
                    any(String.class),
                    any(String.class)
            );
}

@Test
void updateProject_shouldRejectNameExceedingMaximumLength()
        throws Exception {

    UUID workspaceId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    UUID adminId = UUID.randomUUID();

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

    String longName = "A".repeat(101);

    mockMvc.perform(
            put(
                    "/api/workspaces/{workspaceId}/projects/{projectId}",
                    workspaceId,
                    projectId
            )
            .with(user(adminId.toString()))
            .with(csrf())
            .contentType("application/json")
            .content("""
                    {
                        "name": "%s",
                        "description": "Updated project"
                    }
                    """.formatted(longName))
    )
    .andExpect(status().isBadRequest())
    .andExpect(jsonPath(
            "$.status",
            is(400)
    ))
    .andExpect(jsonPath(
            "$.message",
            is("Project name must not exceed 100 characters")
    ));

    verify(projectService, never())
            .findById(projectId);

    verify(projectService, never())
            .updateProject(
                    any(UUID.class),
                    any(String.class),
                    any(String.class)
            );
    }

    private Project createProject(
            UUID projectId,
            UUID workspaceId,
            String name,
            String description,
            ProjectStatus status
    ) {
        Instant now = Instant.now();

        Instant archivedAt =
                status == ProjectStatus.ARCHIVED
                        ? now
                        : null;

        return new Project(
                projectId,
                workspaceId,
                name,
                description,
                status,
                now,
                now,
                archivedAt
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
