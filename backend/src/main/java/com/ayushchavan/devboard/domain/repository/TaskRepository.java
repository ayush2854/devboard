package com.ayushchavan.devboard.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ayushchavan.devboard.domain.entity.Task;

public interface TaskRepository
        extends JpaRepository<Task, UUID> {

    List<Task> findAllByProjectId(UUID projectId);
}
