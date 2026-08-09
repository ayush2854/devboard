package com.ayushchavan.devboard.presentation.controller;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ayushchavan.devboard.application.service.JwtService;
import com.ayushchavan.devboard.application.service.ProjectMembershipService;
import com.ayushchavan.devboard.application.service.ProjectService;
import com.ayushchavan.devboard.application.service.WorkspaceMembershipService;
import com.ayushchavan.devboard.domain.entity.Project;
import com.ayushchavan.devboard.domain.entity.ProjectMembership;
import com.ayushchavan.devboard.domain.entity.ProjectStatus;
import com.ayushchavan.devboard.domain.entity.WorkspaceMembership;
import com.ayushchavan.devboard.domain.entity.WorkspaceRole;

@WebMvcTest(ProjectMembershipController.class)
class ProjectMembershipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectMembershipService projectMembershipService;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private WorkspaceMembershipService membershipService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void getMembers_shouldReturnMembersForWorkspaceMember()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Project project = createProject(
                projectId,
                workspaceId,
                "DevBoard",
                "Project management platform"
        );

        ProjectMembership firstMembership =
                createMembership(
                        projectId,
                        UUID.randomUUID()
                );

        ProjectMembership secondMembership =
                createMembership(
                        projectId,
                        UUID.randomUUID()
                );

        WorkspaceMembership workspaceMembership =
                createWorkspaceMembership(
                        workspaceId,
                        userId,
                        WorkspaceRole.MEMBER
                );

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                userId
        )).thenReturn(java.util.Optional.of(
                workspaceMembership
        ));

        when(projectService.findById(projectId))
                .thenReturn(project);

        when(projectMembershipService.findAllByProjectId(
                projectId
        )).thenReturn(List.of(
                firstMembership,
                secondMembership
        ));

        mockMvc.perform(
                get(
                        "/api/workspaces/{workspaceId}/projects/{projectId}/members",
                        workspaceId,
                        projectId
                )
                .with(user(userId.toString()))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()", is(2)))
        .andExpect(jsonPath(
                "$[0].projectId",
                is(projectId.toString())
        ))
        .andExpect(jsonPath(
                "$[0].userId",
                is(firstMembership.getUserId().toString())
        ))
        .andExpect(jsonPath(
                "$[1].userId",
                is(secondMembership.getUserId().toString())
        ));
    }

    @Test
    void getMembers_shouldRejectNonWorkspaceMember()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                userId
        )).thenReturn(java.util.Optional.empty());

        mockMvc.perform(
                get(
                        "/api/workspaces/{workspaceId}/projects/{projectId}/members",
                        workspaceId,
                        projectId
                )
                .with(user(userId.toString()))
        )
        .andExpect(status().isForbidden());

        verify(projectService, never())
                .findById(projectId);

        verify(projectMembershipService, never())
                .findAllByProjectId(projectId);
    }

    @Test
    void getMembers_shouldRejectProjectFromAnotherWorkspace()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID anotherWorkspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Project project = createProject(
                projectId,
                anotherWorkspaceId,
                "Other Project",
                "Other workspace project"
        );

        WorkspaceMembership workspaceMembership =
                createWorkspaceMembership(
                        workspaceId,
                        userId,
                        WorkspaceRole.MEMBER
                );

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                userId
        )).thenReturn(java.util.Optional.of(
                workspaceMembership
        ));

        when(projectService.findById(projectId))
                .thenReturn(project);

        mockMvc.perform(
                get(
                        "/api/workspaces/{workspaceId}/projects/{projectId}/members",
                        workspaceId,
                        projectId
                )
                .with(user(userId.toString()))
        )
        .andExpect(status().isForbidden());

        verify(projectMembershipService, never())
                .findAllByProjectId(projectId);
    }

    @Test
    void addMember_shouldAllowAdmin()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        Project project = createProject(
                projectId,
                workspaceId,
                "DevBoard",
                "Project management platform"
        );

        ProjectMembership membership =
                createMembership(
                        projectId,
                        memberId
                );

        WorkspaceMembership workspaceMembership =
                createWorkspaceMembership(
                        workspaceId,
                        adminId,
                        WorkspaceRole.ADMIN
                );

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                adminId
        )).thenReturn(java.util.Optional.of(
                workspaceMembership
        ));

        when(projectService.findById(projectId))
                .thenReturn(project);

        when(projectMembershipService.addMember(
                projectId,
                memberId
        )).thenReturn(membership);

        mockMvc.perform(
                post(
                        "/api/workspaces/{workspaceId}/projects/{projectId}/members",
                        workspaceId,
                        projectId
                )
                .with(user(adminId.toString()))
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                            "userId": "%s"
                        }
                        """.formatted(memberId))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath(
                "$.projectId",
                is(projectId.toString())
        ))
        .andExpect(jsonPath(
                "$.userId",
                is(memberId.toString())
        ));

        verify(projectMembershipService)
                .addMember(projectId, memberId);
    }

    @Test
    void addMember_shouldAllowOwner()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        Project project = createProject(
                projectId,
                workspaceId,
                "DevBoard",
                "Project management platform"
        );

        ProjectMembership membership =
                createMembership(
                        projectId,
                        memberId
                );

        WorkspaceMembership workspaceMembership =
                createWorkspaceMembership(
                        workspaceId,
                        ownerId,
                        WorkspaceRole.OWNER
                );

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                ownerId
        )).thenReturn(java.util.Optional.of(
                workspaceMembership
        ));

        when(projectService.findById(projectId))
                .thenReturn(project);

        when(projectMembershipService.addMember(
                projectId,
                memberId
        )).thenReturn(membership);

        mockMvc.perform(
                post(
                        "/api/workspaces/{workspaceId}/projects/{projectId}/members",
                        workspaceId,
                        projectId
                )
                .with(user(ownerId.toString()))
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                            "userId": "%s"
                        }
                        """.formatted(memberId))
        )
        .andExpect(status().isCreated());

        verify(projectMembershipService)
                .addMember(projectId, memberId);
    }

    @Test
    void addMember_shouldRejectMember()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID newUserId = UUID.randomUUID();

        WorkspaceMembership workspaceMembership =
                createWorkspaceMembership(
                        workspaceId,
                        memberId,
                        WorkspaceRole.MEMBER
                );

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                memberId
        )).thenReturn(java.util.Optional.of(
                workspaceMembership
        ));

        mockMvc.perform(
                post(
                        "/api/workspaces/{workspaceId}/projects/{projectId}/members",
                        workspaceId,
                        projectId
                )
                .with(user(memberId.toString()))
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                            "userId": "%s"
                        }
                        """.formatted(newUserId))
        )
        .andExpect(status().isForbidden());

        verify(projectService, never())
                .findById(projectId);

        verify(projectMembershipService, never())
                .addMember(
                        any(UUID.class),
                        any(UUID.class)
                );
    }

    @Test
    void addMember_shouldRejectProjectFromAnotherWorkspace()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID anotherWorkspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID newUserId = UUID.randomUUID();

        Project project = createProject(
                projectId,
                anotherWorkspaceId,
                "Other Project",
                "Other workspace project"
        );

        WorkspaceMembership workspaceMembership =
                createWorkspaceMembership(
                        workspaceId,
                        adminId,
                        WorkspaceRole.ADMIN
                );

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                adminId
        )).thenReturn(java.util.Optional.of(
                workspaceMembership
        ));

        when(projectService.findById(projectId))
                .thenReturn(project);

        mockMvc.perform(
                post(
                        "/api/workspaces/{workspaceId}/projects/{projectId}/members",
                        workspaceId,
                        projectId
                )
                .with(user(adminId.toString()))
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                            "userId": "%s"
                        }
                        """.formatted(newUserId))
        )
        .andExpect(status().isForbidden());

        verify(projectMembershipService, never())
                .addMember(
                        any(UUID.class),
                        any(UUID.class)
                );
    }

    @Test
    void removeMember_shouldAllowAdmin()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        Project project = createProject(
                projectId,
                workspaceId,
                "DevBoard",
                "Project management platform"
        );

        WorkspaceMembership workspaceMembership =
                createWorkspaceMembership(
                        workspaceId,
                        adminId,
                        WorkspaceRole.ADMIN
                );

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                adminId
        )).thenReturn(java.util.Optional.of(
                workspaceMembership
        ));

        when(projectService.findById(projectId))
                .thenReturn(project);

        mockMvc.perform(
                delete(
                        "/api/workspaces/{workspaceId}/projects/{projectId}/members/{userId}",
                        workspaceId,
                        projectId,
                        memberId
                )
                .with(user(adminId.toString()))
                .with(csrf())
        )
        .andExpect(status().isNoContent());

        verify(projectMembershipService)
                .removeMember(projectId, memberId);
    }

    @Test
    void removeMember_shouldAllowOwner()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        Project project = createProject(
                projectId,
                workspaceId,
                "DevBoard",
                "Project management platform"
        );

        WorkspaceMembership workspaceMembership =
                createWorkspaceMembership(
                        workspaceId,
                        ownerId,
                        WorkspaceRole.OWNER
                );

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                ownerId
        )).thenReturn(java.util.Optional.of(
                workspaceMembership
        ));

        when(projectService.findById(projectId))
                .thenReturn(project);

        mockMvc.perform(
                delete(
                        "/api/workspaces/{workspaceId}/projects/{projectId}/members/{userId}",
                        workspaceId,
                        projectId,
                        memberId
                )
                .with(user(ownerId.toString()))
                .with(csrf())
        )
        .andExpect(status().isNoContent());

        verify(projectMembershipService)
                .removeMember(projectId, memberId);
    }

    @Test
    void removeMember_shouldRejectMember()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();

        WorkspaceMembership workspaceMembership =
                createWorkspaceMembership(
                        workspaceId,
                        memberId,
                        WorkspaceRole.MEMBER
                );

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                memberId
        )).thenReturn(java.util.Optional.of(
                workspaceMembership
        ));

        mockMvc.perform(
                delete(
                        "/api/workspaces/{workspaceId}/projects/{projectId}/members/{userId}",
                        workspaceId,
                        projectId,
                        targetUserId
                )
                .with(user(memberId.toString()))
                .with(csrf())
        )
        .andExpect(status().isForbidden());

        verify(projectService, never())
                .findById(projectId);

        verify(projectMembershipService, never())
                .removeMember(
                        any(UUID.class),
                        any(UUID.class)
                );
    }

    @Test
    void removeMember_shouldRejectProjectFromAnotherWorkspace()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID anotherWorkspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        Project project = createProject(
                projectId,
                anotherWorkspaceId,
                "Other Project",
                "Other workspace project"
        );

        WorkspaceMembership workspaceMembership =
                createWorkspaceMembership(
                        workspaceId,
                        adminId,
                        WorkspaceRole.ADMIN
                );

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                adminId
        )).thenReturn(java.util.Optional.of(
                workspaceMembership
        ));

        when(projectService.findById(projectId))
                .thenReturn(project);

        mockMvc.perform(
                delete(
                        "/api/workspaces/{workspaceId}/projects/{projectId}/members/{userId}",
                        workspaceId,
                        projectId,
                        memberId
                )
                .with(user(adminId.toString()))
                .with(csrf())
        )
        .andExpect(status().isForbidden());

        verify(projectMembershipService, never())
                .removeMember(
                        any(UUID.class),
                        any(UUID.class)
                );
    }

    private Project createProject(
        UUID projectId,
        UUID workspaceId,
        String name,
        String description
    ) {
    Instant now = Instant.now();

    return new Project(
            projectId,
            workspaceId,
            name,
            description,
            ProjectStatus.ACTIVE,
            now,
            now,
            null
    );
    }

    private ProjectMembership createMembership(
            UUID projectId,
            UUID userId
    ) {
        return new ProjectMembership(
                UUID.randomUUID(),
                projectId,
                userId,
                Instant.now()
        );
    }

    private WorkspaceMembership createWorkspaceMembership(
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
