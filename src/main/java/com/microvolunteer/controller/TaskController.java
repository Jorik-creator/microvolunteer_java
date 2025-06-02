package com.microvolunteer.controller;

import com.microvolunteer.dto.request.TaskCreateRequest;
import com.microvolunteer.dto.request.TaskSearchRequest;
import com.microvolunteer.dto.response.TaskResponse;
import com.microvolunteer.enums.TaskStatus;
import com.microvolunteer.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for task operations.
 */
@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Task Management", description = "Operations related to task management")
@SecurityRequirement(name = "keycloak")
public class TaskController {
    
    private final TaskService taskService;
    
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }
    
    @Operation(
        summary = "Create a new task",
        description = "Create a new task. Only users with SENSITIVE role can create tasks."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Task created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "403", description = "Forbidden - only SENSITIVE users can create tasks"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    @PreAuthorize("hasRole('SENSITIVE')")
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody TaskCreateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        
        String keycloakSubject = jwt.getSubject();
        TaskResponse response = taskService.createTask(request, keycloakSubject);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(
        summary = "Search tasks",
        description = "Search tasks with various filters and pagination"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<Page<TaskResponse>> searchTasks(@Valid TaskSearchRequest searchRequest) {
        Page<TaskResponse> tasks = taskService.searchTasks(searchRequest);
        return ResponseEntity.ok(tasks);
    }
    
    @Operation(
        summary = "Get task by ID",
        description = "Get detailed information about a specific task"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(
            @Parameter(description = "Task ID") @PathVariable Long id) {
        
        TaskResponse response = taskService.getTaskById(id);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Get tasks by status",
        description = "Get all tasks filtered by their status"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid task status"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskResponse>> getTasksByStatus(
            @Parameter(description = "Task status") @PathVariable TaskStatus status) {
        
        List<TaskResponse> tasks = taskService.getTasksByStatus(status);
        return ResponseEntity.ok(tasks);
    }
    
    @Operation(
        summary = "Get my tasks",
        description = "Get all tasks created by the current user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/my")
    public ResponseEntity<List<TaskResponse>> getMyTasks(@AuthenticationPrincipal Jwt jwt) {
        String keycloakSubject = jwt.getSubject();
        List<TaskResponse> tasks = taskService.getTasksByAuthor(keycloakSubject);
        
        return ResponseEntity.ok(tasks);
    }
    
    @Operation(
        summary = "Complete a task",
        description = "Mark a task as completed. Only the task author can complete their tasks."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task completed successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - only task author can complete"),
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "409", description = "Task already completed or invalid status transition"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('SENSITIVE')")
    public ResponseEntity<TaskResponse> completeTask(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        
        String keycloakSubject = jwt.getSubject();
        TaskResponse response = taskService.completeTask(id, keycloakSubject);
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Delete a task",
        description = "Delete a task. Only the task author or admin can delete tasks."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - only task author or admin can delete"),
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "409", description = "Cannot delete task with active participants"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        
        String keycloakSubject = jwt.getSubject();
        taskService.deleteTask(id, keycloakSubject);
        
        return ResponseEntity.noContent().build();
    }
    
    @Operation(
        summary = "Get recent tasks",
        description = "Get tasks created in the last specified number of days"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recent tasks retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid days parameter"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/recent")
    public ResponseEntity<List<TaskResponse>> getRecentTasks(
            @Parameter(description = "Number of days") @RequestParam(defaultValue = "7") int days) {
        
        List<TaskResponse> tasks = taskService.getRecentTasks(days);
        return ResponseEntity.ok(tasks);
    }
    
    @Operation(
        summary = "Get tasks with approaching deadline",
        description = "Get tasks with deadline approaching within specified hours"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tasks with approaching deadline retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid hours parameter"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/deadline-approaching")
    public ResponseEntity<List<TaskResponse>> getTasksWithApproachingDeadline(
            @Parameter(description = "Hours until deadline") @RequestParam(defaultValue = "24") int hours) {
        
        List<TaskResponse> tasks = taskService.getTasksWithApproachingDeadline(hours);
        return ResponseEntity.ok(tasks);
    }
    
    @Operation(
        summary = "Get task statistics",
        description = "Get overall task statistics including counts by status"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/statistics")
    public ResponseEntity<TaskService.TaskStatistics> getTaskStatistics() {
        TaskService.TaskStatistics statistics = taskService.getTaskStatistics();
        return ResponseEntity.ok(statistics);
    }
}
