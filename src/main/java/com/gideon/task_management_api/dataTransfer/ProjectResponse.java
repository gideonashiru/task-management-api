package com.gideon.task_management_api.dataTransfer;

import com.gideon.task_management_api.entity.Project;
import com.gideon.task_management_api.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String description,
        UUID ownerId,
        String ownerUsername,
        String ownerName,
        LocalDateTime createdAt
) {
    public static ProjectResponse from(Project project) {
        User owner = project.getOwner();

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                owner != null ? owner.getId() : null,
                owner != null ? owner.getUsername() : null,
                owner != null ? owner.getName() : null,
                project.getCreatedAt()
        );
    }
}
