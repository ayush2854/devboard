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
import java.util.Optional;
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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ayushchavan.devboard.application.service.JwtService;
import com.ayushchavan.devboard.application.service.TeamMembershipService;
import com.ayushchavan.devboard.application.service.TeamService;
import com.ayushchavan.devboard.application.service.WorkspaceMembershipService;
import com.ayushchavan.devboard.domain.entity.Team;
import com.ayushchavan.devboard.domain.entity.TeamMembership;
import com.ayushchavan.devboard.domain.entity.WorkspaceMembership;
import com.ayushchavan.devboard.domain.entity.WorkspaceRole;

@WebMvcTest(TeamMembershipController.class)
class TeamMembershipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TeamMembershipService teamMembershipService;

    @MockitoBean
    private TeamService teamService;

    @MockitoBean
    private WorkspaceMembershipService workspaceMembershipService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void getTeamMembers_shouldReturnMembersForWorkspaceMember()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        Team team = createTeam(
                teamId,
                workspaceId
        );

        TeamMembership membership =
                createMembership(
                        UUID.randomUUID(),
                        teamId,
                        memberId
                );

        WorkspaceMembership workspaceMembership =
                createWorkspaceMembership(
                        workspaceId,
                        userId,
                        WorkspaceRole.MEMBER
                );

        when(workspaceMembershipService
                .findByWorkspaceAndUser(
                        workspaceId,
                        userId
                ))
                .thenReturn(Optional.of(workspaceMembership));

        when(teamService.findById(teamId))
                .thenReturn(team);

        when(teamMembershipService.findAllByTeamId(teamId))
                .thenReturn(List.of(membership));

        mockMvc.perform(
                get(
                        "/api/workspaces/{workspaceId}/teams/{teamId}/members",
                        workspaceId,
                        teamId
                )
                .with(user(userId.toString()))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()", is(1)))
        .andExpect(jsonPath(
                "$[0].id",
                is(membership.getId().toString())
        ))
        .andExpect(jsonPath(
                "$[0].teamId",
                is(teamId.toString())
        ))
        .andExpect(jsonPath(
                "$[0].userId",
                is(memberId.toString())
        ));
    }

    @Test
    void getTeamMembers_shouldRejectNonWorkspaceMember()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(workspaceMembershipService
                .findByWorkspaceAndUser(
                        workspaceId,
                        userId
                ))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                get(
                        "/api/workspaces/{workspaceId}/teams/{teamId}/members",
                        workspaceId,
                        teamId
                )
                .with(user(userId.toString()))
        )
        .andExpect(status().isForbidden());

        verify(teamService, never())
                .findById(teamId);

        verify(teamMembershipService, never())
                .findAllByTeamId(teamId);
    }

    @Test
    void getTeamMembers_shouldRejectTeamFromAnotherWorkspace()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID anotherWorkspaceId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Team team = createTeam(
                teamId,
                anotherWorkspaceId
        );

        WorkspaceMembership membership =
                createWorkspaceMembership(
                        workspaceId,
                        userId,
                        WorkspaceRole.MEMBER
                );

        when(workspaceMembershipService
                .findByWorkspaceAndUser(
                        workspaceId,
                        userId
                ))
                .thenReturn(Optional.of(membership));

        when(teamService.findById(teamId))
                .thenReturn(team);

        mockMvc.perform(
                get(
                        "/api/workspaces/{workspaceId}/teams/{teamId}/members",
                        workspaceId,
                        teamId
                )
                .with(user(userId.toString()))
        )
        .andExpect(status().isForbidden());

        verify(teamMembershipService, never())
                .findAllByTeamId(teamId);
    }

    @Test
    void addTeamMember_shouldReturnCreatedMembershipForAdmin()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID newMemberId = UUID.randomUUID();
        UUID membershipId = UUID.randomUUID();

        Team team = createTeam(
                teamId,
                workspaceId
        );

        TeamMembership membership =
                createMembership(
                        membershipId,
                        teamId,
                        newMemberId
                );

        WorkspaceMembership workspaceMembership =
                createWorkspaceMembership(
                        workspaceId,
                        adminId,
                        WorkspaceRole.ADMIN
                );

        when(workspaceMembershipService
                .findByWorkspaceAndUser(
                        workspaceId,
                        adminId
                ))
                .thenReturn(Optional.of(workspaceMembership));

        when(teamService.findById(teamId))
                .thenReturn(team);

        when(teamMembershipService.addMember(
                teamId,
                newMemberId
        ))
                .thenReturn(membership);

        mockMvc.perform(
                post(
                        "/api/workspaces/{workspaceId}/teams/{teamId}/members",
                        workspaceId,
                        teamId
                )
                .with(user(adminId.toString()))
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                            "userId": "%s"
                        }
                        """.formatted(newMemberId))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath(
                "$.id",
                is(membershipId.toString())
        ))
        .andExpect(jsonPath(
                "$.teamId",
                is(teamId.toString())
        ))
        .andExpect(jsonPath(
                "$.userId",
                is(newMemberId.toString())
        ));

        verify(teamMembershipService)
                .addMember(teamId, newMemberId);
    }

    @Test
    void addTeamMember_shouldRejectWorkspaceMember()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID newMemberId = UUID.randomUUID();

        WorkspaceMembership membership =
                createWorkspaceMembership(
                        workspaceId,
                        memberId,
                        WorkspaceRole.MEMBER
                );

        when(workspaceMembershipService
                .findByWorkspaceAndUser(
                        workspaceId,
                        memberId
                ))
                .thenReturn(Optional.of(membership));

        mockMvc.perform(
                post(
                        "/api/workspaces/{workspaceId}/teams/{teamId}/members",
                        workspaceId,
                        teamId
                )
                .with(user(memberId.toString()))
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                            "userId": "%s"
                        }
                        """.formatted(newMemberId))
        )
        .andExpect(status().isForbidden());

        verify(teamService, never())
                .findById(teamId);

        verify(teamMembershipService, never())
                .addMember(
                        any(UUID.class),
                        any(UUID.class)
                );
    }

    @Test
    void addTeamMember_shouldRejectNonWorkspaceMember()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID newMemberId = UUID.randomUUID();

        when(workspaceMembershipService
                .findByWorkspaceAndUser(
                        workspaceId,
                        userId
                ))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                post(
                        "/api/workspaces/{workspaceId}/teams/{teamId}/members",
                        workspaceId,
                        teamId
                )
                .with(user(userId.toString()))
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                            "userId": "%s"
                        }
                        """.formatted(newMemberId))
        )
        .andExpect(status().isForbidden());

        verify(teamMembershipService, never())
                .addMember(
                        any(UUID.class),
                        any(UUID.class)
                );
    }

    @Test
    void addTeamMember_shouldRejectTeamFromAnotherWorkspace()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID anotherWorkspaceId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID newMemberId = UUID.randomUUID();

        Team team = createTeam(
                teamId,
                anotherWorkspaceId
        );

        WorkspaceMembership membership =
                createWorkspaceMembership(
                        workspaceId,
                        adminId,
                        WorkspaceRole.ADMIN
                );

        when(workspaceMembershipService
                .findByWorkspaceAndUser(
                        workspaceId,
                        adminId
                ))
                .thenReturn(Optional.of(membership));

        when(teamService.findById(teamId))
                .thenReturn(team);

        mockMvc.perform(
                post(
                        "/api/workspaces/{workspaceId}/teams/{teamId}/members",
                        workspaceId,
                        teamId
                )
                .with(user(adminId.toString()))
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                            "userId": "%s"
                        }
                        """.formatted(newMemberId))
        )
        .andExpect(status().isForbidden());

        verify(teamMembershipService, never())
                .addMember(
                        any(UUID.class),
                        any(UUID.class)
                );
    }

    @Test
    void removeTeamMember_shouldReturnNoContentForAdmin()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        Team team = createTeam(
                teamId,
                workspaceId
        );

        WorkspaceMembership membership =
                createWorkspaceMembership(
                        workspaceId,
                        adminId,
                        WorkspaceRole.ADMIN
                );

        when(workspaceMembershipService
                .findByWorkspaceAndUser(
                        workspaceId,
                        adminId
                ))
                .thenReturn(Optional.of(membership));

        when(teamService.findById(teamId))
                .thenReturn(team);

        doNothing().when(teamMembershipService)
                .removeMember(
                        teamId,
                        memberId
                );

        mockMvc.perform(
                delete(
                        "/api/workspaces/{workspaceId}/teams/{teamId}/members/{userId}",
                        workspaceId,
                        teamId,
                        memberId
                )
                .with(user(adminId.toString()))
                .with(csrf())
        )
        .andExpect(status().isNoContent());

        verify(teamMembershipService)
                .removeMember(teamId, memberId);
    }

    @Test
    void removeTeamMember_shouldRejectWorkspaceMember()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();

        WorkspaceMembership membership =
                createWorkspaceMembership(
                        workspaceId,
                        memberId,
                        WorkspaceRole.MEMBER
                );

        when(workspaceMembershipService
                .findByWorkspaceAndUser(
                        workspaceId,
                        memberId
                ))
                .thenReturn(Optional.of(membership));

        mockMvc.perform(
                delete(
                        "/api/workspaces/{workspaceId}/teams/{teamId}/members/{userId}",
                        workspaceId,
                        teamId,
                        targetUserId
                )
                .with(user(memberId.toString()))
                .with(csrf())
        )
        .andExpect(status().isForbidden());

        verify(teamMembershipService, never())
                .removeMember(
                        teamId,
                        targetUserId
                );
    }

    @Test
    void removeTeamMember_shouldRejectNonWorkspaceMember()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();

        when(workspaceMembershipService
                .findByWorkspaceAndUser(
                        workspaceId,
                        userId
                ))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                delete(
                        "/api/workspaces/{workspaceId}/teams/{teamId}/members/{userId}",
                        workspaceId,
                        teamId,
                        targetUserId
                )
                .with(user(userId.toString()))
                .with(csrf())
        )
        .andExpect(status().isForbidden());

        verify(teamMembershipService, never())
                .removeMember(
                        teamId,
                        targetUserId
                );
    }

    @Test
    void removeTeamMember_shouldRejectTeamFromAnotherWorkspace()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID anotherWorkspaceId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        Team team = createTeam(
                teamId,
                anotherWorkspaceId
        );

        WorkspaceMembership membership =
                createWorkspaceMembership(
                        workspaceId,
                        adminId,
                        WorkspaceRole.ADMIN
                );

        when(workspaceMembershipService
                .findByWorkspaceAndUser(
                        workspaceId,
                        adminId
                ))
                .thenReturn(Optional.of(membership));

        when(teamService.findById(teamId))
                .thenReturn(team);

        mockMvc.perform(
                delete(
                        "/api/workspaces/{workspaceId}/teams/{teamId}/members/{userId}",
                        workspaceId,
                        teamId,
                        memberId
                )
                .with(user(adminId.toString()))
                .with(csrf())
        )
        .andExpect(status().isForbidden());

        verify(teamMembershipService, never())
                .removeMember(
                        teamId,
                        memberId
                );
    }

    private Team createTeam(
            UUID teamId,
            UUID workspaceId
    ) {
        Instant now = Instant.now();

        return new Team(
                teamId,
                workspaceId,
                "Backend Team",
                "Backend development team",
                now,
                now
        );
    }

    private TeamMembership createMembership(
            UUID membershipId,
            UUID teamId,
            UUID userId
    ) {
        return new TeamMembership(
                membershipId,
                teamId,
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