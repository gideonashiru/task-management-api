package com.gideon.task_management_api.service;

import com.gideon.task_management_api.dataTransfer.MemberResponse;
import com.gideon.task_management_api.dataTransfer.ProjectResponse;
import com.gideon.task_management_api.entity.Project;
import com.gideon.task_management_api.entity.User;
import com.gideon.task_management_api.exception.DuplicateResourceException;
import com.gideon.task_management_api.exception.ForbiddenException;
import com.gideon.task_management_api.exception.ResourceNotFoundException;
import com.gideon.task_management_api.entity.ProjectMembership;
import com.gideon.task_management_api.entity.ProjectRole;
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
import static org.mockito.ArgumentMatchers.anyString;
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

        ProjectMembership membership = ProjectMembership.builder()
                .id(UUID.randomUUID())
                .project(newProject)
                .member(owner)
                .role(ProjectRole.OWNER)
                .build();

        when(projectRepository.save(any(Project.class))).thenReturn(newProject);
        when(projectMembershipRepository.save(any(ProjectMembership.class))).thenReturn(membership);

        // Act
        ProjectResponse result = projectService.createProject(owner, "Test Project", "Test Description");

        // Assert
        assertEquals(projectId, result.id());
        assertEquals("Test Project", result.name());
        assertEquals("Test Description", result.description());
        assertEquals(owner.getId(), result.ownerId());
        assertEquals(owner.getUsername(), result.ownerUsername());
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void getProjectById_shouldReturnProject_whenProjectExists() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        User projectMember = User.builder().id(UUID.randomUUID()).build();
        Project project = Project.builder()
                .id(projectId)
                .name("Test Project")
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMembershipRepository.findByProjectIdAndMemberId(projectId, projectMember.getId()))
                .thenReturn(Optional.of(ProjectMembership.builder()
                        .id(UUID.randomUUID())
                        .project(project)
                        .member(projectMember)
                        .role(ProjectRole.MEMBER)
                        .build()));

        // Act
        ProjectResponse result = projectService.getProjectById(projectId, projectMember);

        // Assert
        assertEquals(projectId, result.id());
        verify(projectRepository).findById(projectId);
    }

    @Test
    void getProjectById_shouldThrowException_whenUserIsNotAMember() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        User notAMember = User.builder().id(UUID.randomUUID()).build();
        Project project = Project.builder()
                .id(projectId)
                .name("Test Project")
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMembershipRepository.findByProjectIdAndMemberId(projectId, notAMember.getId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ForbiddenException.class, () -> {
            projectService.getProjectById(projectId, notAMember);
        });
        verify(projectRepository).findById(projectId);
    }

    @Test
    void getProjectById_shouldThrowException_whenProjectDoesNotExist() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        User projectMember = User.builder().id(UUID.randomUUID()).build();
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            projectService.getProjectById(projectId, projectMember);
        });
        verify(projectRepository).findById(projectId);
    }

    @Test
    void updateProject_shouldThrowException_whenProjectDoesNotExist() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        User requestingUser = User.builder().id(UUID.randomUUID()).build();
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            projectService.updateProject(projectId, requestingUser, "New Name", "New Description");
        });

        verify(projectRepository, never()).save(any());
    }

    @Test
    void updateProject_shouldThrowException_whenReqUserIsNotOwner() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        User notOwner = User.builder().id(UUID.randomUUID()).build();
        Project project = Project.builder()
                .id(projectId)
                .name("Test Project")
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMembershipRepository.findByProjectIdAndMemberId(projectId, notOwner.getId()))
                .thenReturn(Optional.of(ProjectMembership.builder()
                        .id(UUID.randomUUID())
                        .project(project)
                        .member(notOwner)
                        .role(ProjectRole.MEMBER)
                        .build()));

        // Act & Assert
        assertThrows(ForbiddenException.class, () -> {
            projectService.updateProject(projectId, notOwner, "New Name", "New Description");
        });

        verify(projectRepository, never()).save(any());
    }

    @Test
    void updateProject_shouldUpdateProjectSuccessfully_whenReqUserIsOwner() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        User owner = User.builder().id(UUID.randomUUID()).build();
        Project project = Project.builder()
                .id(projectId)
                .name("Test Project")
                .build();

        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMembershipRepository.findByProjectIdAndMemberId(projectId, owner.getId()))
                .thenReturn(Optional.of(ProjectMembership.builder()
                        .id(UUID.randomUUID())
                        .project(project)
                        .member(owner)
                        .role(ProjectRole.OWNER)
                        .build()));

        // Act
        ProjectResponse result = projectService.updateProject(projectId, owner, "New Name", "New Description");

        // Assert
        assertEquals("New Name", result.name());
        assertEquals("New Description", result.description());
        verify(projectRepository).findById(projectId);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void deleteProject_isSuccessfulWhenUserIsOwner() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        User owner = User.builder().id(UUID.randomUUID()).build();
        Project project = Project.builder()
                .id(projectId)
                .name("Test Project")
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMembershipRepository.findByProjectIdAndMemberId(projectId, owner.getId()))
                .thenReturn(Optional.of(ProjectMembership.builder()
                        .id(UUID.randomUUID())
                        .project(project)
                        .member(owner)
                        .role(ProjectRole.OWNER)
                        .build()));

        // Act
        projectService.deleteProject(projectId, owner);

        // Assert
        verify(projectRepository).deleteById(projectId);
    }

    @Test
    void deleteProject_shouldThrowException_whenProjectDoesNotExist() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows( ResourceNotFoundException.class, () -> {
            projectService.deleteProject(projectId, User.builder().id(UUID.randomUUID()).build());
        });

        verify(projectRepository).findById(projectId);
        verify(projectRepository, never()).deleteById(any());
    }

    @Test
    void deleteProject_shouldThrowException_whenUserIsNotOwner() {

        // Arrange
        UUID projectId = UUID.randomUUID();
        User notOwner = User.builder().id(UUID.randomUUID()).build();
        Project project = Project.builder()
                .id(projectId)
                .name("Test Project")
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMembershipRepository.findByProjectIdAndMemberId(projectId, notOwner.getId()))
                .thenReturn(Optional.of(ProjectMembership.builder()
                        .id(UUID.randomUUID())
                        .project(project)
                        .member(notOwner)
                        .role(ProjectRole.MEMBER)
                        .build()));

        // Act & Assert
        assertThrows(ForbiddenException.class, () -> {
            projectService.deleteProject(projectId, notOwner);
        });

        verify(projectRepository).findById(projectId);
        verify(projectRepository, never()).deleteById(any());

    }

    // TESTS FOR ADDING MEMBERS TO PROJECTS
    @Test
    void addMember_shouldThrowException_whenProjectDoesNotExist() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        User owner = User.builder().id(UUID.randomUUID()).build();
        User newMember = User.builder().id(UUID.randomUUID()).build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            projectService.addMember(projectId, owner, newMember.getUsername());
        });

        verify(projectRepository).findById(projectId);
        verify(userRepository, never()).findByUsername(anyString());
        verify(projectMembershipRepository, never()).save(any());
    }

    @Test
    void addMember_shouldThrowException_whenRequestingUserIsNotOwner() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        User notOwner = User.builder().id(UUID.randomUUID()).build();
        User newMember = User.builder().id(UUID.randomUUID()).build();
        Project project = Project.builder()
                .id(projectId)
                .name("Test Project")
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMembershipRepository.findByProjectIdAndMemberId(projectId, notOwner.getId()))
                .thenReturn(Optional.of(ProjectMembership.builder()
                        .id(UUID.randomUUID())
                        .project(project)
                        .member(notOwner)
                        .role(ProjectRole.MEMBER)
                        .build()));

        // Act & Assert
        assertThrows(ForbiddenException.class, () -> {
            projectService.addMember(projectId, notOwner, newMember.getUsername());
        });

        verify(projectRepository).findById(projectId);
        verify(projectMembershipRepository).findByProjectIdAndMemberId(projectId, notOwner.getId());
        verify(userRepository, never()).findByUsername(anyString());
        verify(projectMembershipRepository, never()).save(any());
    }

    @Test
    void addMember_shouldThrowException_whenUserToAddDoesNotExist() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        User owner = User.builder().id(UUID.randomUUID()).build();
        String newMemberUsername = "newMember";

        Project project = Project.builder()
                .id(projectId)
                .name("Test Project")
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMembershipRepository.findByProjectIdAndMemberId(projectId, owner.getId()))
                .thenReturn(Optional.of(ProjectMembership.builder()
                        .id(UUID.randomUUID())
                        .project(project)
                        .member(owner)
                        .role(ProjectRole.OWNER)
                        .build()));
        when(userRepository.findByUsername(newMemberUsername)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            projectService.addMember(projectId, owner, newMemberUsername);
        });

        verify(projectRepository).findById(projectId);
        verify(projectMembershipRepository).findByProjectIdAndMemberId(projectId, owner.getId());
        verify(userRepository).findByUsername(newMemberUsername);
        verify(projectMembershipRepository, never()).save(any());
    }

    @Test
    void addMember_shouldThrow_whenUserisalreadyAMember() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        User owner = User.builder().id(UUID.randomUUID()).build();
        User existingMember = User.builder().id(UUID.randomUUID()).build();
        String existingMemberUsername = "existingMember";

        Project project = Project.builder()
                .id(projectId)
                .name("Test Project")
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMembershipRepository.findByProjectIdAndMemberId(projectId, owner.getId()))
                .thenReturn(Optional.of(ProjectMembership.builder()
                        .id(UUID.randomUUID())
                        .project(project)
                        .member(owner)
                        .role(ProjectRole.OWNER)
                        .build()));
        when(userRepository.findByUsername(existingMemberUsername)).thenReturn(Optional.of(existingMember));
        when(projectMembershipRepository.existsByProjectIdAndMemberId(projectId, existingMember.getId()))
                .thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            projectService.addMember(projectId, owner, existingMemberUsername);
        });

        verify(projectRepository).findById(projectId);
        verify(projectMembershipRepository).findByProjectIdAndMemberId(projectId, owner.getId());
        verify(userRepository).findByUsername(existingMemberUsername);
        verify(projectMembershipRepository).existsByProjectIdAndMemberId(projectId, existingMember.getId());
        verify(projectMembershipRepository, never()).save(any());

    }

    @Test
    void addMember_shouldSuccessfullyAddMember_whenAllInputsAreValid() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        User owner = User.builder().id(UUID.randomUUID()).build();
        User newMember = User.builder().id(UUID.randomUUID()).build();
        String newMemberUsername = "newMember";

        Project project = Project.builder()
                .id(projectId)
                .name("Test Project")
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMembershipRepository.findByProjectIdAndMemberId(projectId, owner.getId()))
                .thenReturn(Optional.of(ProjectMembership.builder()
                        .id(UUID.randomUUID())
                        .project(project)
                        .member(owner)
                        .role(ProjectRole.OWNER)
                        .build()));
        when(userRepository.findByUsername(newMemberUsername)).thenReturn(Optional.of(newMember));
        when(projectMembershipRepository.existsByProjectIdAndMemberId(projectId, newMember.getId()))
                .thenReturn(false);
        when(projectMembershipRepository.save(any(ProjectMembership.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        projectService.addMember(projectId, owner, newMemberUsername);

        // Assert
        verify(projectRepository).findById(projectId);
        verify(projectMembershipRepository).findByProjectIdAndMemberId(projectId, owner.getId());
        verify(userRepository).findByUsername(newMemberUsername);
        verify(projectMembershipRepository).existsByProjectIdAndMemberId(projectId, newMember.getId());
        verify(projectMembershipRepository).save(any(ProjectMembership.class));
    }

    // REMOVE MEMBER TESTS
    @Test
    void removeMember_shouldThrowException_whenProjectDoesNotExist() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        User owner = User.builder().id(UUID.randomUUID()).build();
        User memberToRemove = User.builder().id(UUID.randomUUID()).build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            projectService.removeMember(projectId, owner, memberToRemove.getUsername());
        });

        verify(projectRepository).findById(projectId);
        verify(userRepository, never()).findByUsername(anyString());
        verify(projectMembershipRepository, never()).delete(any());

    }

    @Test
    void removeMember_shouldThrowException_whenRequesterIsNotProjectOwner() {

        UUID projectId = UUID.randomUUID();
        User notOwner = User.builder().id(UUID.randomUUID()).build();
        User memberToRemove = User.builder().id(UUID.randomUUID()).build();
        Project project = Project.builder()
                .id(projectId)
                .name("Test Project")
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMembershipRepository.findByProjectIdAndMemberId(projectId, notOwner.getId()))
                .thenReturn(Optional.of(ProjectMembership.builder()
                        .id(UUID.randomUUID())
                        .project(project)
                        .member(notOwner)
                        .role(ProjectRole.MEMBER)
                        .build()));

        // Act & Assert
        assertThrows(ForbiddenException.class, () -> {
            projectService.removeMember(projectId, notOwner, memberToRemove.getUsername());
        });

        verify(projectRepository).findById(projectId);
        verify(projectMembershipRepository).findByProjectIdAndMemberId(projectId, notOwner.getId());
        verify(projectMembershipRepository, never()).delete(any());
    }

    @Test
    void removeMember_shouldThrowException_whenMemberDoesNotExist() {

        UUID projectId = UUID.randomUUID();
        User owner = User.builder().id(UUID.randomUUID()).build();
        String nonExistentMemberUsername = "nonExistentMember";

        Project project = Project.builder()
                .id(projectId)
                .name("Test Project")
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMembershipRepository.findByProjectIdAndMemberId(projectId, owner.getId()))
                .thenReturn(Optional.of(ProjectMembership.builder()
                        .id(UUID.randomUUID())
                        .project(project)
                        .member(owner)
                        .role(ProjectRole.OWNER)
                        .build()));
        when(userRepository.findByUsername(nonExistentMemberUsername)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            projectService.removeMember(projectId, owner, nonExistentMemberUsername);
        });

        verify(projectRepository).findById(projectId);
        verify(projectMembershipRepository).findByProjectIdAndMemberId(projectId, owner.getId());
        verify(userRepository).findByUsername(nonExistentMemberUsername);
        verify(projectMembershipRepository, never()).delete(any());

    }

    @Test
    void removeMember_shouldThrowException_whenMemberIsNotInProject() {

        UUID projectId = UUID.randomUUID();
        User owner = User.builder().id(UUID.randomUUID()).build();
        User nonMember = User.builder().id(UUID.randomUUID()).build();
        String nonMemberUsername = "nonMember";

        Project project = Project.builder()
                .id(projectId)
                .name("Test Project")
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMembershipRepository.findByProjectIdAndMemberId(projectId, owner.getId()))
                .thenReturn(Optional.of(ProjectMembership.builder()
                        .id(UUID.randomUUID())
                        .project(project)
                        .member(owner)
                        .role(ProjectRole.OWNER)
                        .build()));
        when(userRepository.findByUsername(nonMemberUsername)).thenReturn(Optional.of(nonMember));
        when(projectMembershipRepository.findByProjectIdAndMemberId(projectId, nonMember.getId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            projectService.removeMember(projectId, owner, nonMemberUsername);
        });

        verify(projectRepository).findById(projectId);
        verify(projectMembershipRepository).findByProjectIdAndMemberId(projectId, owner.getId());
        verify(userRepository).findByUsername(nonMemberUsername);
        verify(projectMembershipRepository).findByProjectIdAndMemberId(projectId, nonMember.getId());
        verify(projectMembershipRepository, never()).delete(any());
    }

    @Test
    void removeMember_shouldReturnSuccess_whenAllInputsAreValid() {

        UUID projectId = UUID.randomUUID();
        User owner = User.builder().id(UUID.randomUUID()).build();
        User memberToRemove = User.builder().id(UUID.randomUUID()).build();
        String memberToRemoveUsername = "memberToRemove";

        Project project = Project.builder()
                .id(projectId)
                .name("Test Project")
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMembershipRepository.findByProjectIdAndMemberId(projectId, owner.getId()))
                .thenReturn(Optional.of(ProjectMembership.builder()
                        .id(UUID.randomUUID())
                        .project(project)
                        .member(owner)
                        .role(ProjectRole.OWNER)
                        .build()));
        when(userRepository.findByUsername(memberToRemoveUsername)).thenReturn(Optional.of(memberToRemove));
        when(projectMembershipRepository.findByProjectIdAndMemberId(projectId, memberToRemove.getId()))
                .thenReturn(Optional.of(ProjectMembership.builder()
                        .id(UUID.randomUUID())
                        .project(project)
                        .member(memberToRemove)
                        .role(ProjectRole.MEMBER)
                        .build()));

        // Act
        Boolean result = projectService.removeMember(projectId, owner, memberToRemoveUsername);
        verify(projectMembershipRepository).delete(any());
        assertTrue(result);
    }



    // TESTING FOR GET PROJECT MEMBERS
    @Test
    void getProjectMembers_shouldThrowException_whenProjectDoesNotExist() {

        UUID projectId = UUID.randomUUID();
        User requestingUser = User.builder().id(UUID.randomUUID()).build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            projectService.getProjectMembers(projectId, requestingUser);
        });

        verify(projectRepository).findById(projectId);
    }

    @Test
    void getProjectMembers_shouldThrowException_whenRequestingUserIsNotAMember() {

        UUID projectId = UUID.randomUUID();
        User notAMember = User.builder().id(UUID.randomUUID()).build();
        Project project = Project.builder()
                .id(projectId)
                .name("Test Project")
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMembershipRepository.findByProjectIdAndMemberId(projectId, notAMember.getId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ForbiddenException.class, () -> {
            projectService.getProjectMembers(projectId, notAMember);
        });

        verify(projectRepository).findById(projectId);
        verify(projectMembershipRepository).findByProjectIdAndMemberId(projectId, notAMember.getId());
        verify(projectMembershipRepository, never()).findByProjectId(any());
    }

    @Test
    void getProjectMembers_shouldReturnMembers_whenProjectExistsAndUserIsAMember() {

        UUID projectId = UUID.randomUUID();
        User member1 = User.builder().id(UUID.randomUUID()).build();
        User member2 = User.builder().id(UUID.randomUUID()).build();
        User requestingUser = member1;

        Project project = Project.builder()
                .id(projectId)
                .name("Test Project")
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMembershipRepository.findByProjectIdAndMemberId(projectId, requestingUser.getId()))
                .thenReturn(Optional.of(ProjectMembership.builder()
                        .id(UUID.randomUUID())
                        .project(project)
                        .member(requestingUser)
                        .role(ProjectRole.MEMBER)
                        .build()));
        when(projectMembershipRepository.findByProjectId(projectId))
                .thenReturn(List.of(
                        ProjectMembership.builder().id(UUID.randomUUID()).project(project).member(member1)
                                .role(ProjectRole.MEMBER).build(),
                        ProjectMembership.builder().id(UUID.randomUUID()).project(project).member(member2)
                                .role(ProjectRole.MEMBER).build()));

        // Act
        List<MemberResponse> result = projectService.getProjectMembers(projectId, requestingUser);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(member -> member.id().equals(member1.getId())));
        assertTrue(result.stream().anyMatch(member -> member.id().equals(member2.getId())));
        verify(projectRepository).findById(projectId);
        verify(projectMembershipRepository).findByProjectIdAndMemberId(projectId, requestingUser.getId());
        verify(projectMembershipRepository).findByProjectId(projectId);
    }
}
