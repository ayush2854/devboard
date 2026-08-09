package com.ayushchavan.devboard.application.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ayushchavan.devboard.domain.entity.ProjectMembership;
import com.ayushchavan.devboard.domain.repository.ProjectMembershipRepository;

@ExtendWith(MockitoExtension.class)
class ProjectMembershipServiceTest {

    @Mock
    private ProjectMembershipRepository projectMembershipRepository;

    @InjectMocks
    private ProjectMembershipService projectMembershipService;

    private UUID projectId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    void findAllByProjectId_shouldReturnProjectMembers() {

        ProjectMembership firstMembership =
                createMembership(
                        UUID.randomUUID(),
                        projectId,
                        UUID.randomUUID()
                );

        ProjectMembership secondMembership =
                createMembership(
                        UUID.randomUUID(),
                        projectId,
                        UUID.randomUUID()
                );

        when(projectMembershipRepository.findAllByProjectId(projectId))
                .thenReturn(List.of(
                        firstMembership,
                        secondMembership
                ));

        List<ProjectMembership> result =
                projectMembershipService.findAllByProjectId(
                        projectId
                );

        assertEquals(2, result.size());
        assertEquals(
                firstMembership,
                result.get(0)
        );
        assertEquals(
                secondMembership,
                result.get(1)
        );

        verify(projectMembershipRepository)
                .findAllByProjectId(projectId);
    }

    @Test
    void findByProjectAndUser_shouldReturnMembership() {

        ProjectMembership membership =
                createMembership(
                        UUID.randomUUID(),
                        projectId,
                        userId
                );

        when(projectMembershipRepository
                .findByProjectIdAndUserId(
                        projectId,
                        userId
                ))
                .thenReturn(Optional.of(membership));

        ProjectMembership result =
                projectMembershipService.findByProjectAndUser(
                        projectId,
                        userId
                );

        assertEquals(membership, result);

        verify(projectMembershipRepository)
                .findByProjectIdAndUserId(
                        projectId,
                        userId
                );
    }

    @Test
    void findByProjectAndUser_shouldThrowWhenMembershipDoesNotExist() {

        when(projectMembershipRepository
                .findByProjectIdAndUserId(
                        projectId,
                        userId
                ))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> projectMembershipService
                                .findByProjectAndUser(
                                        projectId,
                                        userId
                                )
                );

        assertEquals(
                "Project membership not found",
                exception.getMessage()
        );
    }

    @Test
    void addMember_shouldCreateAndSaveMembership() {

        ProjectMembership savedMembership =
                createMembership(
                        UUID.randomUUID(),
                        projectId,
                        userId
                );

        when(projectMembershipRepository
                .existsByProjectIdAndUserId(
                        projectId,
                        userId
                ))
                .thenReturn(false);

        when(projectMembershipRepository.save(
                org.mockito.ArgumentMatchers.any(
                        ProjectMembership.class
                )
        ))
                .thenReturn(savedMembership);

        ProjectMembership result =
                projectMembershipService.addMember(
                        projectId,
                        userId
                );

        assertEquals(savedMembership, result);

        verify(projectMembershipRepository)
                .existsByProjectIdAndUserId(
                        projectId,
                        userId
                );

        verify(projectMembershipRepository)
                .save(
                        org.mockito.ArgumentMatchers.any(
                                ProjectMembership.class
                        )
                );
    }

    @Test
    void addMember_shouldGenerateMembershipId() {

        when(projectMembershipRepository
                .existsByProjectIdAndUserId(
                        projectId,
                        userId
                ))
                .thenReturn(false);

        when(projectMembershipRepository.save(
                org.mockito.ArgumentMatchers.any(
                        ProjectMembership.class
                )
        ))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        ProjectMembership result =
                projectMembershipService.addMember(
                        projectId,
                        userId
                );

        assertEquals(projectId, result.getProjectId());
        assertEquals(userId, result.getUserId());
        assertEquals(
                projectId != null,
                result.getProjectId() != null
        );
        assertEquals(
                userId != null,
                result.getUserId() != null
        );
    }

    @Test
    void addMember_shouldRejectDuplicateMembership() {

        when(projectMembershipRepository
                .existsByProjectIdAndUserId(
                        projectId,
                        userId
                ))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> projectMembershipService.addMember(
                                projectId,
                                userId
                        )
                );

        assertEquals(
                "User is already a member of this project",
                exception.getMessage()
        );

        verify(projectMembershipRepository, never())
                .save(
                        org.mockito.ArgumentMatchers.any(
                                ProjectMembership.class
                        )
                );
    }

    @Test
    void removeMember_shouldDeleteMembership() {

        ProjectMembership membership =
                createMembership(
                        UUID.randomUUID(),
                        projectId,
                        userId
                );

        when(projectMembershipRepository
                .findByProjectIdAndUserId(
                        projectId,
                        userId
                ))
                .thenReturn(Optional.of(membership));

        projectMembershipService.removeMember(
                projectId,
                userId
        );

        verify(projectMembershipRepository)
                .findByProjectIdAndUserId(
                        projectId,
                        userId
                );

        verify(projectMembershipRepository)
                .delete(membership);
    }

    @Test
    void removeMember_shouldThrowWhenMembershipDoesNotExist() {

        when(projectMembershipRepository
                .findByProjectIdAndUserId(
                        projectId,
                        userId
                ))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> projectMembershipService.removeMember(
                                projectId,
                                userId
                        )
                );

        assertEquals(
                "Project membership not found",
                exception.getMessage()
        );

        verify(projectMembershipRepository, never())
                .delete(
                        org.mockito.ArgumentMatchers.any(
                                ProjectMembership.class
                        )
                );
    }

    private ProjectMembership createMembership(
            UUID membershipId,
            UUID projectId,
            UUID userId
    ) {
        return new ProjectMembership(
                membershipId,
                projectId,
                userId,
                Instant.now()
        );
    }
}