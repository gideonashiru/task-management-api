package com.gideon.task_management_api.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.gideon.task_management_api.dataTransfer.TaskResponse;
import com.gideon.task_management_api.entity.Project;
import com.gideon.task_management_api.entity.Task;
import com.gideon.task_management_api.entity.TaskStatus;
import com.gideon.task_management_api.entity.User;
import com.gideon.task_management_api.exception.ForbiddenException;
import com.gideon.task_management_api.exception.InvalidStateException;
import com.gideon.task_management_api.exception.ResourceNotFoundException;
import com.gideon.task_management_api.repository.ProjectMembershipRepository;
import com.gideon.task_management_api.repository.ProjectRepository;
import com.gideon.task_management_api.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final ProjectMembershipRepository projectMembershipRepository;

    public TaskResponse createTask(UUID projectId, String title, String description, User creator) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        projectMembershipRepository.findByProjectIdAndMemberId(projectId, creator.getId())
                .orElseThrow(() -> new ForbiddenException("Creator must be a member of the project"));

        Task task = Task.builder()
                .project(project)
                .title(title)
                .description(description)
                .build();

        return TaskResponse.from(taskRepository.save(task));
    }

    public TaskResponse assignTask(UUID taskId, User assignee) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        projectMembershipRepository.findByProjectIdAndMemberId(task.getProject().getId(), assignee.getId())
                .orElseThrow(() -> new ForbiddenException("Assignee must be a member of the project"));

        if (task.getAssignee() != null || task.getStatus() != TaskStatus.TODO) {
            throw new InvalidStateException("Task is already assigned or not in TODO status");
        }

        task.setAssignee(assignee);
        task.setStatus(TaskStatus.IN_PROGRESS);

        return TaskResponse.from(taskRepository.save(task));
    }

    public TaskResponse updateTaskStatus(UUID taskId, User requester) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (task.getAssignee() == null || !task.getAssignee().getId().equals(requester.getId())) {
            throw new ForbiddenException("Only the assignee can update the task status");
        }

        if (task.getStatus() != TaskStatus.IN_PROGRESS) {
            throw new InvalidStateException("Task must be in IN_PROGRESS status to be marked as DONE");
        }

        task.setStatus(TaskStatus.DONE);
        return TaskResponse.from(taskRepository.save(task));
    }

    public List<TaskResponse> getTasksByProject(UUID projectId, User requester) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        projectMembershipRepository.findByProjectIdAndMemberId(projectId, requester.getId())
                .orElseThrow(() -> new ForbiddenException("Requester must be a member of the project"));

        return taskRepository.findByProjectId(project.getId()).stream()
                .map(TaskResponse::from)
                .toList();
    }
}