package com.gideon.task_management_api.dataTransfer;

import com.gideon.task_management_api.entity.Project;
import com.gideon.task_management_api.entity.Task;
import com.gideon.task_management_api.entity.TaskStatus;
import com.gideon.task_management_api.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        UUID projectId,
        UUID assigneeId,
        String assigneeUsername,
        String assigneeName,
        String title,
        String description,
        TaskStatus status,
        LocalDateTime createdAt
) {
    public static TaskResponse from(Task task) {
        Project project = task.getProject();
        User assignee = task.getAssignee();

        return new TaskResponse(
                task.getId(),
                project != null ? project.getId() : null,
                assignee != null ? assignee.getId() : null,
                assignee != null ? assignee.getUsername() : null,
                assignee != null ? assignee.getName() : null,
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getCreatedAt()
        );
    }
}
