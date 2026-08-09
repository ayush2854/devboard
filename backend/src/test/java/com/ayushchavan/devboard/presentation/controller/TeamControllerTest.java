package com.ayushchavan.devboard.presentation.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ayushchavan.devboard.application.service.JwtService;
import com.ayushchavan.devboard.application.service.TeamService;
import com.ayushchavan.devboard.application.service.WorkspaceMembershipService;
import com.ayushchavan.devboard.domain.entity.Team;
import com.ayushchavan.devboard.domain.entity.WorkspaceMembership;
import com.ayushchavan.devboard.domain.entity.WorkspaceRole;

@WebMvcTest(TeamController.class)
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TeamService teamService;

    @MockitoBean
    private WorkspaceMembershipService membershipService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void getTeams_shouldReturnTeamsForWorkspaceMember()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Team backendTeam = createTeam(
                UUID.randomUUID(),
                workspaceId,
                "Backend Team",
                "Backend development team"
        );

        Team frontendTeam = createTeam(
                UUID.randomUUID(),
                workspaceId,
                "Frontend Team",
                "Frontend development team"
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
        )).thenReturn(java.util.Optional.of(membership));

        when(teamService.findAllByWorkspaceId(workspaceId))
                .thenReturn(List.of(
                        backendTeam,
                        frontendTeam
                ));

        mockMvc.perform(
                get(
                        "/api/workspaces/{workspaceId}/teams",
                        workspaceId
                )
                .with(user(userId.toString()))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()", is(2)))
        .andExpect(jsonPath(
                "$[0].id",
                is(backendTeam.getId().toString())
        ))
        .andExpect(jsonPath(
                "$[0].name",
                is("Backend Team")
        ))
        .andExpect(jsonPath(
                "$[1].id",
                is(frontendTeam.getId().toString())
        ))
        .andExpect(jsonPath(
                "$[1].name",
                is("Frontend Team")
        ));
    }

    @Test
    void getTeams_shouldRejectNonWorkspaceMember()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                userId
        )).thenReturn(java.util.Optional.empty());

        mockMvc.perform(
                get(
                        "/api/workspaces/{workspaceId}/teams",
                        workspaceId
                )
                .with(user(userId.toString()))
        )
        .andExpect(status().isForbidden());

        verify(teamService, never())
                .findAllByWorkspaceId(workspaceId);
    }

    @Test
    void getTeam_shouldReturnTeamForWorkspaceMember()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Team team = createTeam(
                teamId,
                workspaceId,
                "Backend Team",
                "Backend development team"
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
        )).thenReturn(java.util.Optional.of(membership));

        when(teamService.findById(teamId))
                .thenReturn(team);

        mockMvc.perform(
                get(
                        "/api/workspaces/{workspaceId}/teams/{teamId}",
                        workspaceId,
                        teamId
                )
                .with(user(userId.toString()))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath(
                "$.id",
                is(teamId.toString())
        ))
        .andExpect(jsonPath(
                "$.workspaceId",
                is(workspaceId.toString())
        ))
        .andExpect(jsonPath(
                "$.name",
                is("Backend Team")
        ))
        .andExpect(jsonPath(
                "$.description",
                is("Backend development team")
        ));
    }

    @Test
    void getTeam_shouldRejectNonWorkspaceMember()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                userId
        )).thenReturn(java.util.Optional.empty());

        mockMvc.perform(
                get(
                        "/api/workspaces/{workspaceId}/teams/{teamId}",
                        workspaceId,
                        teamId
                )
                .with(user(userId.toString()))
        )
        .andExpect(status().isForbidden());

        verify(teamService, never())
                .findById(teamId);
    }

    @Test
    void createTeam_shouldReturnCreatedTeamForAdmin()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();

        Team team = createTeam(
                teamId,
                workspaceId,
                "Backend Team",
                "Backend development team"
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
        )).thenReturn(java.util.Optional.of(membership));

        when(teamService.createTeam(
                workspaceId,
                "Backend Team",
                "Backend development team"
        )).thenReturn(team);

        mockMvc.perform(
                post(
                        "/api/workspaces/{workspaceId}/teams",
                        workspaceId
                )
                .with(user(adminId.toString()))
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                            "name": "Backend Team",
                            "description": "Backend development team"
                        }
                        """)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath(
                "$.id",
                is(teamId.toString())
        ))
        .andExpect(jsonPath(
                "$.workspaceId",
                is(workspaceId.toString())
        ))
        .andExpect(jsonPath(
                "$.name",
                is("Backend Team")
        ))
        .andExpect(jsonPath(
                "$.description",
                is("Backend development team")
        ));
    }

    @Test
    void createTeam_shouldRejectMember()
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
        )).thenReturn(java.util.Optional.of(membership));

        mockMvc.perform(
                post(
                        "/api/workspaces/{workspaceId}/teams",
                        workspaceId
                )
                .with(user(memberId.toString()))
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                            "name": "Backend Team",
                            "description": "Backend development team"
                        }
                        """)
        )
        .andExpect(status().isForbidden());

        verify(teamService, never())
                .createTeam(
                        any(UUID.class),
                        any(String.class),
                        any(String.class)
                );
    }

    @Test
    void updateTeam_shouldReturnUpdatedTeamForAdmin()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        Team team = createTeam(
                teamId,
                workspaceId,
                "Updated Backend Team",
                "Updated description"
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
        )).thenReturn(java.util.Optional.of(membership));

        when(teamService.findById(teamId))
                .thenReturn(team);

        when(teamService.updateTeam(
                teamId,
                "Updated Backend Team",
                "Updated description"
        )).thenReturn(team);

        mockMvc.perform(
                put(
                        "/api/workspaces/{workspaceId}/teams/{teamId}",
                        workspaceId,
                        teamId
                )
                .with(user(adminId.toString()))
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                            "name": "Updated Backend Team",
                            "description": "Updated description"
                        }
                        """)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath(
                "$.id",
                is(teamId.toString())
        ))
        .andExpect(jsonPath(
                "$.name",
                is("Updated Backend Team")
        ))
        .andExpect(jsonPath(
                "$.description",
                is("Updated description")
        ));
    }

    @Test
    void updateTeam_shouldRejectMember()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
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
        )).thenReturn(java.util.Optional.of(membership));

        mockMvc.perform(
                put(
                        "/api/workspaces/{workspaceId}/teams/{teamId}",
                        workspaceId,
                        teamId
                )
                .with(user(memberId.toString()))
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                            "name": "Updated Team",
                            "description": "Updated description"
                        }
                        """)
        )
        .andExpect(status().isForbidden());

        verify(teamService, never())
                .updateTeam(
                        any(UUID.class),
                        any(String.class),
                        any(String.class)
                );
    }

    @Test
    void deleteTeam_shouldReturnNoContentForAdmin()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        Team team = createTeam(
                teamId,
                workspaceId,
                "Backend Team",
                "Backend development team"
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
        )).thenReturn(java.util.Optional.of(membership));

        when(teamService.findById(teamId))
                .thenReturn(team);

        doNothing().when(teamService)
                .deleteTeam(teamId);

        mockMvc.perform(
                delete(
                        "/api/workspaces/{workspaceId}/teams/{teamId}",
                        workspaceId,
                        teamId
                )
                .with(user(adminId.toString()))
                .with(csrf())
        )
        .andExpect(status().isNoContent());

        verify(teamService)
                .deleteTeam(teamId);
    }

    @Test
    void deleteTeam_shouldRejectMember()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
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
        )).thenReturn(java.util.Optional.of(membership));

        mockMvc.perform(
                delete(
                        "/api/workspaces/{workspaceId}/teams/{teamId}",
                        workspaceId,
                        teamId
                )
                .with(user(memberId.toString()))
                .with(csrf())
        )
        .andExpect(status().isForbidden());

        verify(teamService, never())
                .deleteTeam(teamId);
    }

    private Team createTeam(
            UUID teamId,
            UUID workspaceId,
            String name,
            String description
    ) {
        Instant now = Instant.now();

        return new Team(
                teamId,
                workspaceId,
                name,
                description,
                now,
                now
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