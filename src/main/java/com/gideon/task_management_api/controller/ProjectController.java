package com.gideon.task_management_api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gideon.task_management_api.dataTransfer.AddMemberRequest;
import com.gideon.task_management_api.dataTransfer.CreateProjectRequest;
import com.gideon.task_management_api.dataTransfer.MemberResponse;
import com.gideon.task_management_api.dataTransfer.ProjectResponse;
import com.gideon.task_management_api.entity.User;
import com.gideon.task_management_api.service.ProjectService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal User currentUser) {
        ProjectResponse response = projectService.createProject(currentUser, request.name(), request.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        ProjectResponse response = projectService.getProjectById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal User currentUser) {
        ProjectResponse response = projectService.updateProject(id, currentUser, request.name(), request.description());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        projectService.deleteProject(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<Void> addMember(
            @PathVariable UUID id,
            @Valid @RequestBody AddMemberRequest request,
            @AuthenticationPrincipal User currentUser) {
        projectService.addMember(id, currentUser, request.username());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}/members/{username}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID id,
            @PathVariable String username,
            @AuthenticationPrincipal User currentUser) {
        projectService.removeMember(id, currentUser, username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<MemberResponse>> getProjectMembers(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        List<MemberResponse> members = projectService.getProjectMembers(id, currentUser);
        return ResponseEntity.ok(members);
    }
}

