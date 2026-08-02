package com.gideon.task_management_api.dataTransfer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(

        @NotBlank(message = "Project name is required") @Size(max = 100, message = "Project name must not exceed 100 characters") String name,

        @Size(max = 3000, message = "Project description must not exceed 3000 characters") String description) {

}
