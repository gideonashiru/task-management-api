package com.gideon.task_management_api.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gideon.task_management_api.dataTransfer.MemberResponse;
import com.gideon.task_management_api.dataTransfer.ProjectResponse;
import com.gideon.task_management_api.entity.Project;
import com.gideon.task_management_api.entity.ProjectMembership;
import com.gideon.task_management_api.entity.ProjectRole;
import com.gideon.task_management_api.entity.User;
import com.gideon.task_management_api.exception.DuplicateResourceException;
import com.gideon.task_management_api.exception.ForbiddenException;
import com.gideon.task_management_api.exception.ResourceNotFoundException;
import com.gideon.task_management_api.repository.ProjectMembershipRepository;
import com.gideon.task_management_api.repository.ProjectRepository;
import com.gideon.task_management_api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMembershipRepository projectMembershipRepository;

    public ProjectResponse createProject(User owner, String name, String description) {

        Project project = Project.builder()
                .owner(owner)
                .name(name.trim())
                .description(description)
                .build();

        Project savedProject = projectRepository.save(project);

        ProjectMembership ownerMembership = ProjectMembership.builder()
                .project(savedProject)
                .member(owner)
                .role(ProjectRole.OWNER)
                .build();

        projectMembershipRepository.save(ownerMembership);

        return ProjectResponse.from(savedProject);
    }

    public ProjectResponse getProjectById(UUID projectId, User requestingUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (projectMembershipRepository.findByProjectIdAndMemberId(projectId, requestingUser.getId()).isEmpty()) {
            throw new ForbiddenException("Access denied");
        }

        return ProjectResponse.from(project);
    }

    public ProjectResponse updateProject(UUID projectId, User requestingUser, String name, String description) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        ProjectMembership membership = projectMembershipRepository
                .findByProjectIdAndMemberId(projectId, requestingUser.getId())
                .orElseThrow(() -> new ForbiddenException("Access denied"));

        if (membership.getRole() != ProjectRole.OWNER) {
            throw new ForbiddenException("Access denied");
        }
        if (name != null && !name.trim().isEmpty()) {
            project.setName(name.trim());
        }
        if (description != null) {
            project.setDescription(description);
        }

        return ProjectResponse.from(projectRepository.save(project));
    }

    public void deleteProject(UUID projectId, User requestingUser) {

        projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        ProjectMembership membership = projectMembershipRepository
                .findByProjectIdAndMemberId(projectId, requestingUser.getId())
                .orElseThrow(() -> new ForbiddenException("Access denied"));

        if (membership.getRole() != ProjectRole.OWNER) {
            throw new ForbiddenException("Access denied");
        }

        projectMembershipRepository.deleteById(membership.getId());
        projectRepository.deleteById(projectId);
    }

    public void addMember(UUID projectId, User requestingUser, String newMemberUsername) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        ProjectMembership membership = projectMembershipRepository
                .findByProjectIdAndMemberId(projectId, requestingUser.getId())
                .orElseThrow(() -> new ForbiddenException("Access denied"));

        if (membership.getRole() != ProjectRole.OWNER) {
            throw new ForbiddenException("Access denied");
        }

        User newMember = userRepository.findByUsername(newMemberUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (projectMembershipRepository.existsByProjectIdAndMemberId(projectId, newMember.getId())) {
            throw new DuplicateResourceException("User is already a member of this project");
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
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        ProjectMembership ownerMembership = projectMembershipRepository
                .findByProjectIdAndMemberId(projectId, requestingUser.getId())
                .orElseThrow(() -> new ForbiddenException("User is not a member of this project"));

        if (ownerMembership.getRole() != ProjectRole.OWNER) {
            throw new ForbiddenException("Only project owners can remove members");
        }

        User memberToRemove = userRepository.findByUsername(memberUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ProjectMembership membershipToRemove = projectMembershipRepository
                .findByProjectIdAndMemberId(projectId, memberToRemove.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this project"));

        projectMembershipRepository.delete(membershipToRemove);
        return true;
    }

    public List<MemberResponse> getProjectMembers(UUID projectId, User requestingUser) {

        projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        projectMembershipRepository
                .findByProjectIdAndMemberId(projectId, requestingUser.getId())
                .orElseThrow(() -> new ForbiddenException("Access denied"));

        return projectMembershipRepository.findByProjectId(projectId).stream()
                .map(MemberResponse::from)
                .collect(Collectors.toList());
    }

}