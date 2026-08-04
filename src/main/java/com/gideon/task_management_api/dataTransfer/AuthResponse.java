package com.gideon.task_management_api.dataTransfer;

import java.util.UUID;

import com.gideon.task_management_api.entity.User;

public record AuthResponse(
                String token,
                UUID userId,
                String username,
                String name) {

        public static AuthResponse from(User user, String token) {
                return new AuthResponse(
                                token,
                                user.getId(),
                                user.getUsername(),
                                user.getName());
        }

}