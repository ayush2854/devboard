package com.ayushchavan.devboard.application.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ayushchavan.devboard.application.exception.ConflictException;
import com.ayushchavan.devboard.application.exception.ResourceNotFoundException;
import com.ayushchavan.devboard.domain.entity.WorkspaceMembership;
import com.ayushchavan.devboard.domain.entity.WorkspaceRole;
import com.ayushchavan.devboard.domain.repository.WorkspaceMembershipRepository;

@ExtendWith(MockitoExtension.class)
class WorkspaceMembershipServiceTest {

    @Mock
    private WorkspaceMembershipRepository membershipRepository;

    @InjectMocks
    private WorkspaceMembershipService membershipService;

    @Test
    void findByWorkspaceAndUser_shouldReturnMembershipWhenExists() {
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        WorkspaceMembership membership =
                new WorkspaceMembership(
                        UUID.randomUUID(),
                        workspaceId,
                        userId,
                        WorkspaceRole.MEMBER,
                        Instant.now()
                );

        when(membershipRepository.findByWorkspaceIdAndUserId(
                workspaceId,
                userId
        )).thenReturn(Optional.of(membership));

        Optional<WorkspaceMembership> result =
                membershipService.findByWorkspaceAndUser(
                        workspaceId,
                        userId
                );

        assertTrue(result.isPresent());
        assertEquals(
                workspaceId,
                result.get().getWorkspaceId()
        );
        assertEquals(
                userId,
                result.get().getUserId()
        );
        assertEquals(
                WorkspaceRole.MEMBER,
                result.get().getRole()
        );

        verify(membershipRepository)
                .findByWorkspaceIdAndUserId(
                        workspaceId,
                        userId
                );
    }

    @Test
    void isMember_shouldReturnTrueWhenMembershipExists() {
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(membershipRepository.existsByWorkspaceIdAndUserId(
                workspaceId,
                userId
        )).thenReturn(true);

        boolean result = membershipService.isMember(
                workspaceId,
                userId
        );

        assertTrue(result);

        verify(membershipRepository)
                .existsByWorkspaceIdAndUserId(
                        workspaceId,
                        userId
                );
    }

    @Test
    void createMembership_shouldCreateAndSaveMembership() {
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(membershipRepository.existsByWorkspaceIdAndUserId(
                workspaceId,
                userId
        )).thenReturn(false);

        when(membershipRepository.save(
                any(WorkspaceMembership.class)
        )).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        WorkspaceMembership result =
                membershipService.createMembership(
                        workspaceId,
                        userId,
                        WorkspaceRole.MEMBER
                );

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(
                workspaceId,
                result.getWorkspaceId()
        );
        assertEquals(
                userId,
                result.getUserId()
        );
        assertEquals(
                WorkspaceRole.MEMBER,
                result.getRole()
        );
        assertNotNull(result.getJoinedAt());

        verify(membershipRepository)
                .existsByWorkspaceIdAndUserId(
                        workspaceId,
                        userId
                );

        verify(membershipRepository)
                .save(any(WorkspaceMembership.class));
    }

    @Test
    void createMembership_shouldRejectDuplicateMembership() {
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(membershipRepository.existsByWorkspaceIdAndUserId(
                workspaceId,
                userId
        )).thenReturn(true);

        ConflictException exception =
                assertThrows(
                        ConflictException.class,
                        () -> membershipService.createMembership(
                                workspaceId,
                                userId,
                                WorkspaceRole.MEMBER
                        )
                );

        assertEquals(
                "User is already a member of this workspace",
                exception.getMessage()
        );

        verify(membershipRepository)
                .existsByWorkspaceIdAndUserId(
                        workspaceId,
                        userId
                );

        verify(membershipRepository, never())
                .save(any(WorkspaceMembership.class));
    }

    @Test
    void createMembership_shouldRejectNullRole() {
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(membershipRepository.existsByWorkspaceIdAndUserId(
                workspaceId,
                userId
        )).thenReturn(false);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> membershipService.createMembership(
                                workspaceId,
                                userId,
                                null
                        )
                );

        assertEquals(
                "Workspace role is required",
                exception.getMessage()
        );

        verify(membershipRepository)
                .existsByWorkspaceIdAndUserId(
                        workspaceId,
                        userId
                );

        verify(membershipRepository, never())
                .save(any(WorkspaceMembership.class));
    }

    @Test
    void deleteMembership_shouldDeleteMembership() {
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        WorkspaceMembership membership =
                new WorkspaceMembership(
                        UUID.randomUUID(),
                        workspaceId,
                        userId,
                        WorkspaceRole.MEMBER,
                        Instant.now()
                );

        when(membershipRepository.findByWorkspaceIdAndUserId(
                workspaceId,
                userId
        )).thenReturn(Optional.of(membership));

        membershipService.deleteMembership(
                workspaceId,
                userId
        );

        verify(membershipRepository)
                .findByWorkspaceIdAndUserId(
                        workspaceId,
                        userId
                );

        verify(membershipRepository)
                .delete(membership);
    }

    @Test
    void deleteMembership_shouldRejectWhenMembershipDoesNotExist() {
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(membershipRepository.findByWorkspaceIdAndUserId(
                workspaceId,
                userId
        )).thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> membershipService.deleteMembership(
                                workspaceId,
                                userId
                        )
                );

        assertEquals(
                "Workspace membership not found",
                exception.getMessage()
        );

        verify(membershipRepository)
                .findByWorkspaceIdAndUserId(
                        workspaceId,
                        userId
                );

        verify(membershipRepository, never())
                .delete(any(WorkspaceMembership.class));
    }
}
