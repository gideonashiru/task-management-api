package com.gideon.task_management_api.repository;

import com.gideon.task_management_api.entity.ProjectMembership;
import com.gideon.task_management_api.entity.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMembershipRepository extends JpaRepository<ProjectMembership, UUID> {

    boolean existsByProjectIdAndMemberId(UUID projectId, UUID memberId);

    // Optional<Project> findByProjectId(UUID projectId);

    List<ProjectMembership> findByProjectId(UUID projectId);

    Optional<ProjectMembership> findByProjectIdAndMemberId(UUID projectId, UUID memberId);

    Optional<ProjectMembership> findByProjectIdAndMemberIdAndRole(
            UUID projectId, UUID memberId, ProjectRole role);

}
