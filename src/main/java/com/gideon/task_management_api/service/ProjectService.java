package com.gideon.task_management_api.service;

import com.gideon.task_management_api.entity.Project;
import com.gideon.task_management_api.entity.User;
import com.gideon.task_management_api.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Project createProject(User owner, String name, String description) {
        if (owner == null) {
            throw new IllegalArgumentException("Owner cannot be null");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Project name cannot be empty");
        }

        Project project = Project.builder()
                .owner(owner)
                .name(name.trim())
                .description(description)
                .build();

        return projectRepository.save(project);
    }

    public Optional<Project> getProjectById(UUID id) {
        return projectRepository.findById(id);
    }

    public List<Project> getProjectsByOwnerId(UUID ownerId) {
        return projectRepository.findByOwnerId(ownerId);
    }

    public List<Project> getProjectsByName(String name) {
        return projectRepository.findByName(name);
    }

    public Project updateProject(UUID id, String name, String description) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        if (name != null && !name.trim().isEmpty()) {
            project.setName(name.trim());
        }
        if (description != null) {
            project.setDescription(description);
        }

        return projectRepository.save(project);
    }

    public void deleteProject(UUID id) {
        projectRepository.deleteById(id);
    }
}
