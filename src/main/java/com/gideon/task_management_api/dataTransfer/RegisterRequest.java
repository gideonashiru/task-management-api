package com.gideon.task_management_api.dataTransfer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Username is required") @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters") String username,

        @NotBlank(message = "Password is required") @Size(min = 8, max = 100, message = "Password must be at least 8 characters") String password,

        @NotBlank(message = "Name is required") @Size(max = 100, message = "Name must not exceed 100 characters") String name) {

}
