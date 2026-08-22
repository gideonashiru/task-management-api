package com.gideon.task_management_api.service;

import com.gideon.task_management_api.dataTransfer.TaskResponse;
import com.gideon.task_management_api.entity.Project;
import com.gideon.task_management_api.entity.User;
import com.gideon.task_management_api.entity.ProjectMembership;
import com.gideon.task_management_api.entity.ProjectRole;
import com.gideon.task_management_api.entity.Task;
import com.gideon.task_management_api.entity.TaskStatus;
import com.gideon.task_management_api.exception.ForbiddenException;
import com.gideon.task_management_api.exception.InvalidStateException;
import com.gideon.task_management_api.exception.ResourceNotFoundException;
import com.gideon.task_management_api.repository.ProjectRepository;
import com.gideon.task_management_api.repository.ProjectMembershipRepository;
import com.gideon.task_management_api.repository.TaskRepository;

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
public class TaskServiceTest {

    @InjectMocks
    TaskService taskService;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectMembershipRepository projectMembershipRepository;
    @Mock
    private TaskRepository taskRepository;

    @Test
    void createTask_shouldThrowException_whenProjectDoesNotExist() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        User creator = User.builder().id(UUID.randomUUID()).build();
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.createTask(projectId, "Task Name", "Task Description", creator);
        });
    }

    @Test
    void createTask_shouldThrowException_whenCreatorisNotProjectMember() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        User creator = User.builder().id(UUID.randomUUID()).build();
        Project project = Project.builder().id(projectId).build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMembershipRepository.findByProjectIdAndMemberId(projectId, creator.getId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ForbiddenException.class, () -> {
            taskService.createTask(projectId, "Task Name", "Task Description", creator);
        });
    }

    @Test
    void createTask_shouldCreateTaskSuccessfully_whenProjectExistsAndUserIsMember() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        User creator = User.builder().id(UUID.randomUUID()).build();
        Project project = Project.builder().id(projectId).build();
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMembershipRepository.findByProjectIdAndMemberId(projectId, creator.getId()))
                .thenReturn(Optional.of(ProjectMembership.builder()
                        .project(project)
                        .member(creator)
                        .role(ProjectRole.MEMBER)
                        .build()));
        when(taskRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TaskResponse result = taskService.createTask(projectId, "Task Name", "Task Description", creator);

        // Assert
        assertNotNull(result);
        assertEquals("Task Name", result.title());
        assertEquals("Task Description", result.description());
        assertEquals(projectId, result.projectId());
    }

    // assign task test cases

    @Test
    void assignTask_shouldThrowException_whenTaskDoesNotExist() {
        // Arrange
        UUID taskId = UUID.randomUUID();
        User assignee = User.builder().id(UUID.randomUUID()).build();
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.assignTask(taskId, assignee);
        });
        verify(taskRepository, never()).save(any());
    }

    @Test
    void assignTask_shouldThrowException_whenAssigneeIsNotProjectMember() {
        // Arrange
        UUID taskId = UUID.randomUUID();
        User assignee = User.builder().id(UUID.randomUUID()).build();
        Project project = Project.builder().id(UUID.randomUUID()).build();
        Task task = Task.builder().id(taskId).project(project).build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(projectMembershipRepository.findByProjectIdAndMemberId(task.getProject().getId(), assignee.getId()))
                .thenReturn(Optional.empty());
        // Act & Assert
        assertThrows(ForbiddenException.class, () -> {
            taskService.assignTask(taskId, assignee);
        });

        verify(taskRepository, never()).save(any());
    }

    @Test
    void assignTask_shouldThrowException_whenTaskisnotInTodoStatus() {

        // Arrange
        UUID taskId = UUID.randomUUID();
        User assignee = User.builder().id(UUID.randomUUID()).build();
        Project project = Project.builder().id(UUID.randomUUID()).build();
        Task task = Task.builder().id(taskId).project(project).status(TaskStatus.IN_PROGRESS).build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(projectMembershipRepository.findByProjectIdAndMemberId(task.getProject().getId(), assignee.getId()))
                .thenReturn(Optional.of(ProjectMembership.builder()
                        .project(task.getProject())
                        .member(assignee)
                        .role(ProjectRole.MEMBER)
                        .build()));
        // Act & Assert
        assertThrows(InvalidStateException.class, () -> {
            taskService.assignTask(taskId, assignee);
        });
        verify(taskRepository, never()).save(any());
    }

    @Test
    void assignTask_shouldThrowException_whenTaskisAlreadyAssigned() {

        // Arrange
        UUID taskId = UUID.randomUUID();
        User assignee = User.builder().id(UUID.randomUUID()).build();
        User existingAssignee = User.builder().id(UUID.randomUUID()).build();
        Project project = Project.builder().id(UUID.randomUUID()).build();
        Task task = Task.builder().id(taskId).project(project).assignee(existingAssignee).status(TaskStatus.TODO)
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(projectMembershipRepository.findByProjectIdAndMemberId(task.getProject().getId(), assignee.getId()))
                .thenReturn(Optional.of(ProjectMembership.builder()
                        .project(task.getProject())
                        .member(assignee)
                        .role(ProjectRole.MEMBER)
                        .build()));
        // Act & Assert
        assertThrows(InvalidStateException.class, () -> {
            taskService.assignTask(taskId, assignee);
        });
        verify(taskRepository, never()).save(any());
    }

    @Test
    void assignTask_shouldAssignTaskSuccessfully_whenTaskExistsAndAssigneeIsProjectMember() {
        // Arrange
        UUID taskId = UUID.randomUUID();
        User assignee = User.builder().id(UUID.randomUUID()).build();
        Project project = Project.builder().id(UUID.randomUUID()).build();
        Task task = Task.builder().id(taskId).project(project).status(TaskStatus.TODO).build();
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(projectMembershipRepository.findByProjectIdAndMemberId(task.getProject().getId(), assignee.getId()))
                .thenReturn(Optional.of(ProjectMembership.builder()
                        .project(task.getProject())
                        .member(assignee)
                        .role(ProjectRole.MEMBER)
                        .build()));
        when(taskRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        // Act
        TaskResponse result = taskService.assignTask(taskId, assignee);
        // Assert
        assertNotNull(result);
        assertEquals(assignee.getId(), result.assigneeId());
        assertEquals(TaskStatus.IN_PROGRESS, result.status());
    }

    // update task status

    @Test
    void updateTaskStatus_shouldThrowException_whenTaskDoesNotExist() {
        // Arrange
        UUID taskId = UUID.randomUUID();
        User requester = User.builder().id(UUID.randomUUID()).build();
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.updateTaskStatus(taskId, requester);
        });
        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTaskStatus_shouldThrowException_whenRequesterIsNotAssignedUser() {
        // Arrange
        UUID taskId = UUID.randomUUID();
        User requester = User.builder().id(UUID.randomUUID()).build();
        User assignedUser = User.builder().id(UUID.randomUUID()).build();
        Project project = Project.builder().id(UUID.randomUUID()).build();
        Task task = Task.builder().id(taskId).project(project).assignee(assignedUser).status(TaskStatus.IN_PROGRESS)
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        // Act & Assert
        assertThrows(ForbiddenException.class, () -> {
            taskService.updateTaskStatus(taskId, requester);
        });
        verify(taskRepository, never()).save(any());

    }

    @Test
    void updateTaskStatus_shouldThrowException_whenTaskNotInProgressStatus() {

        // Arrange
        UUID taskId = UUID.randomUUID();
        User requester = User.builder().id(UUID.randomUUID()).build();
        Project project = Project.builder().id(UUID.randomUUID()).build();
        Task task = Task.builder().id(taskId).project(project).status(TaskStatus.TODO).build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        // Act & Assert
        assertThrows(ForbiddenException.class, () -> {
            taskService.updateTaskStatus(taskId, requester);
        });
        verify(taskRepository, never()).save(any());

    }

    @Test
    void updateTaskStatus_shouldUpdateStatusSuccessfully_whenAllConditionsAreMet() {

        // Arrange
        UUID taskId = UUID.randomUUID();
        User requestingUser = User.builder().id(UUID.randomUUID()).build();
        Project project = Project.builder().id(UUID.randomUUID()).build();
        Task task = Task.builder().id(taskId).project(project).assignee(requestingUser).status(TaskStatus.IN_PROGRESS)
                .build();
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        // Act
        TaskResponse result = taskService.updateTaskStatus(taskId, requestingUser);
        // Assert
        assertNotNull(result);
        assertEquals(TaskStatus.DONE, result.status());
    }

    // get tasks by project

    @Test
    void getTasksByProject_shouldThrowException_whenProjectDoesNotExist() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        User requestingUser = User.builder().id(UUID.randomUUID()).build();
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.getTasksByProject(projectId, requestingUser);
        });
    }

    @Test
    void getTasksByProject_shouldThrowException_whenRequesterIsNotProjectMember() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        User requestingUser = User.builder().id(UUID.randomUUID()).build();
        Project project = Project.builder().id(projectId).build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMembershipRepository.findByProjectIdAndMemberId(projectId, requestingUser.getId()))
                .thenReturn(Optional.empty());
        // Act & Assert
        assertThrows(ForbiddenException.class, () -> {
            taskService.getTasksByProject(projectId, requestingUser);
        });

        verify(taskRepository, never()).findByProjectId(any());
    }

    @Test
    void getTasksByProject_shouldReturnTasks_whenAllConditionsAreMet() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        User requestingUser = User.builder().id(UUID.randomUUID()).build();
        Project project = Project.builder().id(projectId).build();
        Task task1 = Task.builder().id(UUID.randomUUID()).project(project).build();
        Task task2 = Task.builder().id(UUID.randomUUID()).project(project).build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(taskRepository.findByProjectId(projectId)).thenReturn(List.of(task1, task2));
        when(projectMembershipRepository.findByProjectIdAndMemberId(projectId, requestingUser.getId()))
                .thenReturn(Optional.of(ProjectMembership.builder()
                        .project(project)
                        .member(requestingUser)
                        .role(ProjectRole.MEMBER)
                        .build()));

        // Act
        List<TaskResponse> result = taskService.getTasksByProject(projectId, requestingUser);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(task -> task.id().equals(task1.getId())));
        assertTrue(result.stream().anyMatch(task -> task.id().equals(task2.getId())));
    }
}
