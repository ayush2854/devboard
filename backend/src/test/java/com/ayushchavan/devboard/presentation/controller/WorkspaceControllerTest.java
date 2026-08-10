package com.ayushchavan.devboard.presentation.controller;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ayushchavan.devboard.application.service.JwtService;
import com.ayushchavan.devboard.application.service.WorkspaceMembershipService;
import com.ayushchavan.devboard.application.service.WorkspaceService;
import com.ayushchavan.devboard.domain.entity.Workspace;
import com.ayushchavan.devboard.domain.entity.WorkspaceMembership;
import com.ayushchavan.devboard.domain.entity.WorkspaceRole;

@WebMvcTest(WorkspaceController.class)
class WorkspaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkspaceService workspaceService;

    @MockitoBean
    private WorkspaceMembershipService membershipService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void createWorkspace_shouldReturnCreatedWorkspace()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();
        Instant now = Instant.now();

        Workspace workspace = new Workspace(
                workspaceId,
                "DevBoard",
                "Project management workspace",
                now,
                now
        );

        when(workspaceService.createWorkspace(
                authenticatedUserId,
                "DevBoard",
                "Project management workspace"
        )).thenReturn(workspace);

        mockMvc.perform(
                post("/api/workspaces")
                        .with(authentication(
                                uuidAuthentication(
                                        authenticatedUserId
                                )
                        ))
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                    "name": "DevBoard",
                                    "description": "Project management workspace"
                                }
                                """)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath(
                "$.id",
                is(workspaceId.toString())
        ))
        .andExpect(jsonPath(
                "$.name",
                is("DevBoard")
        ))
        .andExpect(jsonPath(
                "$.description",
                is("Project management workspace")
        ));
    }

    @Test
    void getWorkspace_shouldReturnWorkspace()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();
        Instant now = Instant.now();

        Workspace workspace = new Workspace(
                workspaceId,
                "DevBoard",
                "Project management workspace",
                now,
                now
        );

        WorkspaceMembership membership =
                new WorkspaceMembership(
                        UUID.randomUUID(),
                        workspaceId,
                        authenticatedUserId,
                        WorkspaceRole.MEMBER,
                        now
                );

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                authenticatedUserId
        )).thenReturn(Optional.of(membership));

        when(workspaceService.findById(workspaceId))
                .thenReturn(Optional.of(workspace));

        mockMvc.perform(
                get(
                        "/api/workspaces/{workspaceId}",
                        workspaceId
                )
                .with(authentication(
                        uuidAuthentication(
                                authenticatedUserId
                        )
                ))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath(
                "$.id",
                is(workspaceId.toString())
        ))
        .andExpect(jsonPath(
                "$.name",
                is("DevBoard")
        ))
        .andExpect(jsonPath(
                "$.description",
                is("Project management workspace")
        ));
    }

    @Test
    void updateWorkspace_shouldReturnUpdatedWorkspace()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();
        Instant now = Instant.now();

        Workspace workspace = new Workspace(
                workspaceId,
                "Updated DevBoard",
                "Updated description",
                now,
                now
        );

        WorkspaceMembership adminMembership =
                new WorkspaceMembership(
                        UUID.randomUUID(),
                        workspaceId,
                        authenticatedUserId,
                        WorkspaceRole.ADMIN,
                        now
                );

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                authenticatedUserId
        )).thenReturn(Optional.of(adminMembership));

        when(workspaceService.updateWorkspace(
                workspaceId,
                "Updated DevBoard",
                "Updated description"
        )).thenReturn(workspace);

        mockMvc.perform(
                put(
                        "/api/workspaces/{workspaceId}",
                        workspaceId
                )
                .with(authentication(
                        uuidAuthentication(
                                authenticatedUserId
                        )
                ))
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                            "name": "Updated DevBoard",
                            "description": "Updated description"
                        }
                        """)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath(
                "$.name",
                is("Updated DevBoard")
        ))
        .andExpect(jsonPath(
                "$.description",
                is("Updated description")
        ));
    }

    @Test
    void deleteWorkspace_shouldReturnNoContent()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();
        Instant now = Instant.now();

        WorkspaceMembership ownerMembership =
                new WorkspaceMembership(
                        UUID.randomUUID(),
                        workspaceId,
                        authenticatedUserId,
                        WorkspaceRole.OWNER,
                        now
                );

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                authenticatedUserId
        )).thenReturn(Optional.of(ownerMembership));

        doNothing().when(workspaceService)
                .deleteWorkspace(workspaceId);

        mockMvc.perform(
                delete(
                        "/api/workspaces/{workspaceId}",
                        workspaceId
                )
                .with(authentication(
                        uuidAuthentication(
                                authenticatedUserId
                        )
                ))
                .with(csrf())
        )
        .andExpect(status().isNoContent());
    }

    @Test
    void getWorkspaceMember_shouldReturnMember()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UUID membershipId = UUID.randomUUID();
        Instant now = Instant.now();

        WorkspaceMembership authenticatedMembership =
                new WorkspaceMembership(
                        UUID.randomUUID(),
                        workspaceId,
                        authenticatedUserId,
                        WorkspaceRole.MEMBER,
                        now
                );

        WorkspaceMembership targetMembership =
                new WorkspaceMembership(
                        membershipId,
                        workspaceId,
                        targetUserId,
                        WorkspaceRole.MEMBER,
                        now
                );

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                authenticatedUserId
        )).thenReturn(Optional.of(authenticatedMembership));

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                targetUserId
        )).thenReturn(Optional.of(targetMembership));

        mockMvc.perform(
                get(
                        "/api/workspaces/{workspaceId}/members/{userId}",
                        workspaceId,
                        targetUserId
                )
                .with(authentication(
                        uuidAuthentication(
                                authenticatedUserId
                        )
                ))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath(
                "$.id",
                is(membershipId.toString())
        ))
        .andExpect(jsonPath(
                "$.workspaceId",
                is(workspaceId.toString())
        ))
        .andExpect(jsonPath(
                "$.userId",
                is(targetUserId.toString())
        ))
        .andExpect(jsonPath(
                "$.role",
                is("MEMBER")
        ));
    }

    @Test
    void addWorkspaceMember_shouldReturnCreatedMemberForAdmin()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID newUserId = UUID.randomUUID();
        UUID membershipId = UUID.randomUUID();
        Instant now = Instant.now();

        WorkspaceMembership adminMembership =
                new WorkspaceMembership(
                        UUID.randomUUID(),
                        workspaceId,
                        adminId,
                        WorkspaceRole.ADMIN,
                        now
                );

        WorkspaceMembership newMembership =
                new WorkspaceMembership(
                        membershipId,
                        workspaceId,
                        newUserId,
                        WorkspaceRole.MEMBER,
                        now
                );

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                adminId
        )).thenReturn(Optional.of(adminMembership));

        when(membershipService.createMembership(
                workspaceId,
                newUserId,
                WorkspaceRole.MEMBER
        )).thenReturn(newMembership);

        mockMvc.perform(
                post(
                        "/api/workspaces/{workspaceId}/members",
                        workspaceId
                )
                .with(authentication(
                        uuidAuthentication(adminId)
                ))
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                            "userId": "%s"
                        }
                        """.formatted(newUserId))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath(
                "$.id",
                is(membershipId.toString())
        ))
        .andExpect(jsonPath(
                "$.userId",
                is(newUserId.toString())
        ))
        .andExpect(jsonPath(
                "$.role",
                is("MEMBER")
        ));
    }

    @Test
    void addWorkspaceMember_shouldRejectMember()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID newUserId = UUID.randomUUID();

        WorkspaceMembership memberMembership =
                new WorkspaceMembership(
                        UUID.randomUUID(),
                        workspaceId,
                        memberId,
                        WorkspaceRole.MEMBER,
                        Instant.now()
                );

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                memberId
        )).thenReturn(Optional.of(memberMembership));

        mockMvc.perform(
                post(
                        "/api/workspaces/{workspaceId}/members",
                        workspaceId
                )
                .with(authentication(
                        uuidAuthentication(memberId)
                ))
                .with(csrf())
                .contentType("application/json")
                .content("""
                        {
                            "userId": "%s"
                        }
                        """.formatted(newUserId))
        )
        .andExpect(status().isForbidden());

        verify(membershipService, never())
                .createMembership(
                        any(UUID.class),
                        any(UUID.class),
                        any(WorkspaceRole.class)
                );
    }

    @Test
    void removeWorkspaceMember_shouldReturnNoContentForAdmin()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();

        WorkspaceMembership adminMembership =
                new WorkspaceMembership(
                        UUID.randomUUID(),
                        workspaceId,
                        adminId,
                        WorkspaceRole.ADMIN,
                        Instant.now()
                );

        WorkspaceMembership targetMembership =
                new WorkspaceMembership(
                        UUID.randomUUID(),
                        workspaceId,
                        targetUserId,
                        WorkspaceRole.MEMBER,
                        Instant.now()
                );

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                adminId
        )).thenReturn(Optional.of(adminMembership));

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                targetUserId
        )).thenReturn(Optional.of(targetMembership));

        doNothing().when(membershipService)
                .deleteMembership(
                        workspaceId,
                        targetUserId
                );

        mockMvc.perform(
                delete(
                        "/api/workspaces/{workspaceId}/members/{userId}",
                        workspaceId,
                        targetUserId
                )
                .with(authentication(
                        uuidAuthentication(adminId)
                ))
                .with(csrf())
        )
        .andExpect(status().isNoContent());

        verify(membershipService)
                .deleteMembership(
                        workspaceId,
                        targetUserId
                );
    }

    @Test
    void removeWorkspaceMember_shouldRejectOwnerRemoval()
            throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        WorkspaceMembership adminMembership =
                new WorkspaceMembership(
                        UUID.randomUUID(),
                        workspaceId,
                        adminId,
                        WorkspaceRole.ADMIN,
                        Instant.now()
                );

        WorkspaceMembership ownerMembership =
                new WorkspaceMembership(
                        UUID.randomUUID(),
                        workspaceId,
                        ownerId,
                        WorkspaceRole.OWNER,
                        Instant.now()
                );

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                adminId
        )).thenReturn(Optional.of(adminMembership));

        when(membershipService.findByWorkspaceAndUser(
                workspaceId,
                ownerId
        )).thenReturn(Optional.of(ownerMembership));

        mockMvc.perform(
                delete(
                        "/api/workspaces/{workspaceId}/members/{userId}",
                        workspaceId,
                        ownerId
                )
                .with(authentication(
                        uuidAuthentication(adminId)
                ))
                .with(csrf())
        )
        .andExpect(status().isForbidden());

        verify(membershipService, never())
                .deleteMembership(
                        workspaceId,
                        ownerId
                );
    }

    private UsernamePasswordAuthenticationToken uuidAuthentication(
            UUID userId
    ) {
        return new UsernamePasswordAuthenticationToken(
                userId,
                null,
                Collections.emptyList()
        );
    }
}
