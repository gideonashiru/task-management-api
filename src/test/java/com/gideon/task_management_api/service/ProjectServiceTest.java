package com.gideon.task_management_api.service;

import com.gideon.task_management_api.entity.Project;
import com.gideon.task_management_api.entity.User;
import com.gideon.task_management_api.repository.ProjectRepository;
import com.gideon.task_management_api.repository.ProjectMembershipRepository;
import com.gideon.task_management_api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMembershipRepository projectMembershipRepository;

    @InjectMocks
    private ProjectService projectService;



    @Test
    void createProject_shouldThrowException_whenProjectNameIsEmpty() {
        // Arrange
        User owner = User.builder().id(UUID.randomUUID()).username("owner").build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            projectService.createProject(owner, "", "Description");
        });

        verify(projectRepository, never()).save(any());
    }

    @Test
    void createProject_shouldSuccessfullyCreateProject_whenAllInputsAreValid() {
        // Arrange
        UUID ownerId = UUID.randomUUID();
        User owner = User.builder().id(ownerId).username("owner").build();
        UUID projectId = UUID.randomUUID();

        Project newProject = Project.builder()
                .id(projectId)
                .owner(owner)
                .name("Test Project")
                .description("Test Description")
                .build();

        when(projectRepository.save(any(Project.class))).thenReturn(newProject);

        // Act
        Project result = projectService.createProject(owner, "Test Project", "Test Description");

        // Assert
        assertEquals(projectId, result.getId());
        assertEquals("Test Project", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertEquals(owner, result.getOwner());
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void getProjectById_shouldReturnProject_whenProjectExists() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        Project project = Project.builder()
                .id(projectId)
                .name("Test Project")
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        // Act
        Optional<Project> result = projectService.getProjectById(projectId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(projectId, result.get().getId());
        verify(projectRepository).findById(projectId);
    }

    @Test
    void getProjectById_shouldReturnEmpty_whenProjectDoesNotExist() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act
        Optional<Project> result = projectService.getProjectById(projectId);

        // Assert
        assertFalse(result.isPresent());
        verify(projectRepository).findById(projectId);
    }

    @Test
    void getProjectsByOwnerId_shouldReturnProjectList_whenProjectsExist() {
        // Arrange
        UUID ownerId = UUID.randomUUID();
        Project project1 = Project.builder().id(UUID.randomUUID()).name("Project 1").build();
        Project project2 = Project.builder().id(UUID.randomUUID()).name("Project 2").build();

        when(projectRepository.findByOwnerId(ownerId)).thenReturn(List.of(project1, project2));

        // Act
        List<Project> result = projectService.getProjectsByOwnerId(ownerId);

        // Assert
        assertEquals(2, result.size());
        verify(projectRepository).findByOwnerId(ownerId);
    }

    @Test
    void updateProject_shouldThrowException_whenProjectDoesNotExist() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            projectService.updateProject(projectId, "New Name", "New Description");
        });

        verify(projectRepository, never()).save(any());
    }

    @Test
    void updateProject_shouldUpdateProjectSuccessfully_whenProjectExists() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        Project existingProject = Project.builder()
                .id(projectId)
                .name("Old Name")
                .description("Old Description")
                .build();

        Project updatedProject = Project.builder()
                .id(projectId)
                .name("New Name")
                .description("New Description")
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject));
        when(projectRepository.save(any(Project.class))).thenReturn(updatedProject);

        // Act
        Project result = projectService.updateProject(projectId, "New Name", "New Description");

        // Assert
        assertEquals("New Name", result.getName());
        assertEquals("New Description", result.getDescription());
        verify(projectRepository).findById(projectId);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void deleteProject_shouldCallRepositoryDelete() {
        // Arrange
        UUID projectId = UUID.randomUUID();

        // Act
        projectService.deleteProject(projectId);

        // Assert
        verify(projectRepository).deleteById(projectId);
    }
}
