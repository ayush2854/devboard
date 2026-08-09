package com.ayushchavan.devboard.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ayushchavan.devboard.domain.entity.Project;

public interface ProjectRepository
        extends JpaRepository<Project, UUID> {

    List<Project> findAllByWorkspaceId(UUID workspaceId);

    boolean existsByWorkspaceIdAndName(
            UUID workspaceId,
            String name
    );
}