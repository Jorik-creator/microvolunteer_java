package com.microvolunteer.controller;

import com.microvolunteer.dto.response.TaskResponse;
import com.microvolunteer.dto.response.UserResponse;
import com.microvolunteer.service.ParticipationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for participation operations.
 */
@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Participation Management", description = "Operations related to volunteer participation in tasks")
@SecurityRequirement(name = "keycloak")
public class ParticipationController {
    
    private final ParticipationService participationService;
    
    public ParticipationController(ParticipationService participationService) {
        this.participationService = participationService;
    }
    
    @Operation(
        summary = "Join a task",
        description = "Join a task as a volunteer. Only users with VOLUNTEER role can participate."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully joined the task"),
        @ApiResponse(responseCode = "403", description = "Forbidden - only volunteers can participate"),
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "409", description = "Already participating or cannot participate"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/{id}/participate")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ResponseEntity<Void> joinTask(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        
        String keycloakSubject = jwt.getSubject();
        participationService.joinTask(id, keycloakSubject);
        
        return ResponseEntity.noContent().build();
    }
    
    @Operation(
        summary = "Leave a task",
        description = "Leave a task as a volunteer. Only current participants can leave."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully left the task"),
        @ApiResponse(responseCode = "403", description = "Forbidden - only volunteers can leave"),
        @ApiResponse(responseCode = "404", description = "Task not found or not participating"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/{id}/participate")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ResponseEntity<Void> leaveTask(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        
        String keycloakSubject = jwt.getSubject();
        participationService.leaveTask(id, keycloakSubject);
        
        return ResponseEntity.noContent().build();
    }
    
    @Operation(
        summary = "Get task volunteers",
        description = "Get list of volunteers participating in a specific task"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Volunteers retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{id}/volunteers")
    public ResponseEntity<List<UserResponse>> getTaskVolunteers(
            @Parameter(description = "Task ID") @PathVariable Long id) {
        
        List<UserResponse> volunteers = participationService.getTaskVolunteers(id);
        return ResponseEntity.ok(volunteers);
    }
    
    @Operation(
        summary = "Check participation status",
        description = "Check if the current user is participating in a specific task"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Participation status retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Task or user not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{id}/participation-status")
    public ResponseEntity<Boolean> checkParticipationStatus(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        
        String keycloakSubject = jwt.getSubject();
        boolean isParticipating = participationService.isUserParticipating(id, keycloakSubject);
        
        return ResponseEntity.ok(isParticipating);
    }
}

