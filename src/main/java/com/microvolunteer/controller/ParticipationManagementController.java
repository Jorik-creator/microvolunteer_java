package com.microvolunteer.controller;

import com.microvolunteer.dto.response.TaskResponse;
import com.microvolunteer.service.ParticipationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List; /**
 * REST controller for participation-related endpoints under /api/participations.
 */
@RestController
@RequestMapping("/api/participations")
@Tag(name = "Participation Management", description = "Operations related to volunteer participation management")
@SecurityRequirement(name = "keycloak")
public class ParticipationManagementController {
    
    private final ParticipationService participationService;
    
    public ParticipationManagementController(ParticipationService participationService) {
        this.participationService = participationService;
    }
    
    @Operation(
        summary = "Get my participating tasks",
        description = "Get all tasks that the current user is participating in"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Participating tasks retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/my-tasks")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ResponseEntity<List<TaskResponse>> getMyParticipatingTasks(@AuthenticationPrincipal Jwt jwt) {
        String keycloakSubject = jwt.getSubject();
        List<TaskResponse> tasks = participationService.getParticipatingTasks(keycloakSubject);
        
        return ResponseEntity.ok(tasks);
    }
    
    @Operation(
        summary = "Get my participation history",
        description = "Get complete participation history for the current volunteer"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Participation history retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/my-history")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ResponseEntity<List<ParticipationService.ParticipationHistory>> getMyParticipationHistory(
            @AuthenticationPrincipal Jwt jwt) {
        
        String keycloakSubject = jwt.getSubject();
        List<ParticipationService.ParticipationHistory> history = 
            participationService.getVolunteerHistory(keycloakSubject);
        
        return ResponseEntity.ok(history);
    }
    
    @Operation(
        summary = "Get my volunteer statistics",
        description = "Get participation statistics for the current volunteer"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Volunteer statistics retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/my-statistics")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ResponseEntity<ParticipationService.VolunteerStatistics> getMyVolunteerStatistics(
            @AuthenticationPrincipal Jwt jwt) {
        
        String keycloakSubject = jwt.getSubject();
        ParticipationService.VolunteerStatistics statistics = 
            participationService.getVolunteerStatistics(keycloakSubject);
        
        return ResponseEntity.ok(statistics);
    }
    
    @Operation(
        summary = "Get volunteer rankings",
        description = "Get rankings of volunteers by participation count"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Volunteer rankings retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/rankings")
    public ResponseEntity<List<ParticipationService.VolunteerRanking>> getVolunteerRankings() {
        List<ParticipationService.VolunteerRanking> rankings = 
            participationService.getVolunteerRankings();
        
        return ResponseEntity.ok(rankings);
    }
}
