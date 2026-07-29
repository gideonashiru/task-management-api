package com.gideon.task_management_api.service;

import com.gideon.task_management_api.entity.Project;
import com.gideon.task_management_api.entity.ProjectMembership;
import com.gideon.task_management_api.entity.User;
import com.gideon.task_management_api.entity.ProjectRole;
import com.gideon.task_management_api.repository.ProjectRepository;
import com.gideon.task_management_api.repository.UserRepository;
import com.gideon.task_management_api.repository.ProjectMembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMembershipRepository projectMembershipRepository;

    public Project createProject(User owner, String name, String description) {
        if (owner == null) {
            throw new IllegalArgumentException("Owner cannot be null");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Project name cannot be empty");
        }

        // Create the project entity
        Project project = Project.builder()
                .owner(owner)
                .name(name.trim())
                .description(description)
                .build();

        // Save the project and get the persisted entity (with ID, etc.)
        Project savedProject = projectRepository.save(project);

        // Create and save the owner's mandatory membership record
        ProjectMembership ownerMembership = ProjectMembership.builder()
                .project(savedProject)
                .member(owner)
                .role(ProjectRole.OWNER)
                .build();

        projectMembershipRepository.save(ownerMembership);

        return savedProject;
    }

    public Project getProjectById(UUID projectId, User requestingUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        if (!projectMembershipRepository.findByProjectIdAndMemberId(projectId,
                requestingUser.getId()).isPresent()) {
            throw new IllegalArgumentException("Access denied");
        }

        return project;
    }

    public Project updateProject(UUID projectId, User requestingUser, String name, String description) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        ProjectMembership membership = projectMembershipRepository
                .findByProjectIdAndMemberId(projectId, requestingUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Access denied"));

        if (membership.getRole() != ProjectRole.OWNER) {
            throw new IllegalArgumentException("Access denied");
        }
        if (name != null && !name.trim().isEmpty()) {
            project.setName(name.trim());
        }
        if (description != null) {
            project.setDescription(description);
        }

        return projectRepository.save(project);
    }

    public void deleteProject(UUID projectId, User requestingUser) {

        projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        ProjectMembership membership = projectMembershipRepository
                .findByProjectIdAndMemberId(projectId, requestingUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Access denied"));

        if (membership.getRole() != ProjectRole.OWNER) {
            throw new IllegalArgumentException("Access denied");
        }

        projectRepository.deleteById(projectId);
    }

    public void addMember(UUID projectId, User requestingUser, String newMemberUsername) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        ProjectMembership membership = projectMembershipRepository
                .findByProjectIdAndMemberId(projectId, requestingUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Access denied"));

        if (membership.getRole() != ProjectRole.OWNER) {
            throw new IllegalArgumentException("Access denied");
        }

        User newMember = userRepository.findByUsername(newMemberUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (projectMembershipRepository.existsByProjectIdAndMemberId(projectId, newMember.getId())) {
            throw new IllegalArgumentException("User is already a member of this project");
        }

        ProjectMembership newMembership = ProjectMembership.builder()
                .project(project)
                .member(newMember)
                .role(ProjectRole.MEMBER)
                .build();

        projectMembershipRepository.save(newMembership);
    }

    public Boolean removeMember(UUID projectId, User requestingUser, String memberUsername) {

        projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        // 2. Check if requesting user is owner
        ProjectMembership ownerMembership = projectMembershipRepository
                .findByProjectIdAndMemberId(projectId, requestingUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this project"));

        if (ownerMembership.getRole() != ProjectRole.OWNER) {
            throw new IllegalArgumentException("Only project owners can remove members");
        }

        // 3. Check if member exists
        User memberToRemove = userRepository.findByUsername(memberUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 4. Check if member is in project
        ProjectMembership membershipToRemove = projectMembershipRepository
                .findByProjectIdAndMemberId(projectId, memberToRemove.getId())
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this project"));

        // 5. Delete the membership
        projectMembershipRepository.delete(membershipToRemove);
        return true; // ← IMPORTANT: Return Boolean, not null
    }

    
public List<User> getProjectMembers(UUID projectId, User requestingUser) {

        projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        projectMembershipRepository
                .findByProjectIdAndMemberId(projectId, requestingUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Access denied"));

        return projectMembershipRepository.findByProjectId(projectId).stream()
                .map(ProjectMembership::getMember)
                .collect(Collectors.toList());
    }

}