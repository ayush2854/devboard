package com.ayushchavan.devboard.domain.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ayushchavan.devboard.domain.entity.Workspace;

public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {
}