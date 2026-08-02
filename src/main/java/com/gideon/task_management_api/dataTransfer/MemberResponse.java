package com.gideon.task_management_api.dataTransfer;

import com.gideon.task_management_api.entity.ProjectMembership;
import com.gideon.task_management_api.entity.ProjectRole;
import com.gideon.task_management_api.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record MemberResponse(
        UUID id,
        String username,
        String name,
        ProjectRole role,
        LocalDateTime joinedAt
) {
    public static MemberResponse from(ProjectMembership membership) {
        User member = membership.getMember();

        return new MemberResponse(
                member != null ? member.getId() : null,
                member != null ? member.getUsername() : null,
                member != null ? member.getName() : null,
                membership.getRole(),
                membership.getJoinedAt()
        );
    }
}
