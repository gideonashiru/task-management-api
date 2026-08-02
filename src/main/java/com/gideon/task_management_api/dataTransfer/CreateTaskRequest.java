package com.gideon.task_management_api.dataTransfer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(

        @NotBlank(message = "Task title is required") @Size(max = 200, message = "Task title must not exceed 200 characters") String title,

        @Size(max = 3000, message = "Task description must not exceed 3000 characters") String description) {

}
