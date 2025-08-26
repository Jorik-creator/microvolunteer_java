package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.TaskRequest;
import org.example.dto.TaskResponse;
import org.example.dto.TaskUpdateRequest;
import org.example.model.TaskStatus;
import org.example.service.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management endpoints")
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    @Operation(summary = "Get all tasks", description = "Get paginated list of all tasks with optional filters")
    public ResponseEntity<Page<TaskResponse>> getAllTasks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTo,
            Pageable pageable) {
        Page<TaskResponse> tasks = taskService.getAllTasks(title, location, categoryId, status, 
                startDateFrom, startDateTo, pageable);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID", description = "Get task details by ID")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        TaskResponse task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @GetMapping("/creator/{creatorId}")
    @Operation(summary = "Get tasks by creator", description = "Get paginated list of tasks created by specific user")
    public ResponseEntity<Page<TaskResponse>> getTasksByCreator(
            @PathVariable Long creatorId,
            Pageable pageable) {
        Page<TaskResponse> tasks = taskService.getTasksByCreator(creatorId, pageable);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/my-tasks")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get current user's created tasks", description = "Get paginated list of tasks created by current user")
    public ResponseEntity<Page<TaskResponse>> getMyTasks(Authentication authentication, Pageable pageable) {
        Page<TaskResponse> tasks = taskService.getTasksByCreatorUsername(authentication.getName(), pageable);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/my-participations")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get current user's task participations", description = "Get paginated list of tasks where current user is a participant")
    public ResponseEntity<Page<TaskResponse>> getMyParticipations(Authentication authentication, Pageable pageable) {
        Page<TaskResponse> tasks = taskService.getTasksByParticipantUsername(authentication.getName(), pageable);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'VULNERABLE')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create task", description = "Create a new task")
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody TaskRequest request,
            Authentication authentication) {
        TaskResponse task = taskService.createTask(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update task", description = "Update task by ID (only task creator)")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskUpdateRequest request,
            Authentication authentication) {
        TaskResponse updatedTask = taskService.updateTask(id, request, authentication.getName());
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete task", description = "Delete task by ID (only task creator)")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id, Authentication authentication) {
        taskService.deleteTask(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/join")
    @PreAuthorize("hasRole('VOLUNTEER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Join task", description = "Join a task as a volunteer")
    public ResponseEntity<TaskResponse> joinTask(@PathVariable Long id, Authentication authentication) {
        TaskResponse task = taskService.joinTask(id, authentication.getName());
        return ResponseEntity.ok(task);
    }

    @DeleteMapping("/{id}/leave")
    @PreAuthorize("hasRole('VOLUNTEER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Leave task", description = "Leave a task as a volunteer")
    public ResponseEntity<TaskResponse> leaveTask(@PathVariable Long id, Authentication authentication) {
        TaskResponse task = taskService.leaveTask(id, authentication.getName());
        return ResponseEntity.ok(task);
    }

    @PatchMapping("/{id}/complete")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Complete task", description = "Mark task as completed (only task creator)")
    public ResponseEntity<TaskResponse> completeTask(@PathVariable Long id, Authentication authentication) {
        TaskResponse task = taskService.completeTask(id, authentication.getName());
        return ResponseEntity.ok(task);
    }

    @PatchMapping("/{id}/cancel")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Cancel task", description = "Cancel task (only task creator)")
    public ResponseEntity<TaskResponse> cancelTask(@PathVariable Long id, Authentication authentication) {
        TaskResponse task = taskService.cancelTask(id, authentication.getName());
        return ResponseEntity.ok(task);
    }

}