package com.ayushchavan.devboard.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ayushchavan.devboard.domain.entity.Team;

public interface TeamRepository extends JpaRepository<Team, UUID> {

    List<Team> findAllByWorkspaceId(UUID workspaceId);

    boolean existsByWorkspaceIdAndName(
            UUID workspaceId,
            String name
    );
}