package com.gideon.task_management_api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gideon.task_management_api.dataTransfer.CreateTaskRequest;
import com.gideon.task_management_api.dataTransfer.TaskResponse;
import com.gideon.task_management_api.entity.User;
import com.gideon.task_management_api.service.TaskService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal User currentUser) {
        TaskResponse response = taskService.createTask(
                projectId, request.title(), request.description(), currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/projects/{projectId}/tasks")
    public ResponseEntity<List<TaskResponse>> getTasksByProject(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal User currentUser) {
        List<TaskResponse> tasks = taskService.getTasksByProject(projectId, currentUser);
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/tasks/{taskId}/assign")
    public ResponseEntity<TaskResponse> assignTask(
            @PathVariable UUID taskId,
            @AuthenticationPrincipal User currentUser) {
        TaskResponse response = taskService.assignTask(taskId, currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/tasks/{taskId}/complete")
    public ResponseEntity<TaskResponse> completeTask(
            @PathVariable UUID taskId,
            @AuthenticationPrincipal User currentUser) {
        TaskResponse response = taskService.updateTaskStatus(taskId, currentUser);
        return ResponseEntity.ok(response);
    }
}
