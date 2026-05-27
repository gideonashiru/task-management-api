package com.gideon.task_management_api.repository;

import com.gideon.task_management_api.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findByName(String name);

    Optional<Project> findById(UUID projectId);
}
