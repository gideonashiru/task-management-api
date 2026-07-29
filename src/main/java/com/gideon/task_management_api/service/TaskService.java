package com.gideon.task_management_api.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.gideon.task_management_api.entity.Project;
import com.gideon.task_management_api.entity.Task;
import com.gideon.task_management_api.entity.TaskStatus;
import com.gideon.task_management_api.entity.User;
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

    public Task createTask(UUID projectId, String title, String description, User creator) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        projectMembershipRepository.findByProjectIdAndMemberId(projectId, creator.getId())
                .orElseThrow(() -> new IllegalArgumentException("Creator must be a member of the project"));

        Task task = Task.builder()
                .project(project)
                .title(title)
                .description(description)
                .build();

        return taskRepository.save(task);
    }

    public Task assignTask(UUID taskId, User assignee) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        projectMembershipRepository.findByProjectIdAndMemberId(task.getProject().getId(), assignee.getId())
                .orElseThrow(() -> new IllegalArgumentException("Assignee must be a member of the project"));

        if (task.getAssignee() != null || task.getStatus() != TaskStatus.TODO) {
            throw new IllegalArgumentException("Task is already assigned or not in TODO status");
        }

        task.setAssignee(assignee);
        task.setStatus(TaskStatus.IN_PROGRESS);

        return taskRepository.save(task);
    }

    // updates to done, only
    public Task updateTaskStatus(UUID taskId, User requester) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (task.getAssignee() == null || !task.getAssignee().getId().equals(requester.getId())) {
            throw new IllegalArgumentException("Only the assignee can update the task status");
        }

        if (!(task.getStatus() == TaskStatus.IN_PROGRESS)) {
            throw new IllegalArgumentException("Task must be in IN_PROGRESS status to be marked as DONE");
        }

        task.setStatus(TaskStatus.DONE);
        return taskRepository.save(task);
    }

    public List<Task> getTasksByProject(UUID projectId, User requester) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        projectMembershipRepository.findByProjectIdAndMemberId(projectId, requester.getId())
                .orElseThrow(() -> new IllegalArgumentException("Requester must be a member of the project"));

        return taskRepository.findByProjectId(project.getId());
    }
}