package com.microvolunteer.controller;

import com.microvolunteer.entity.Participation;
import com.microvolunteer.service.ParticipationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/participations")
@RequiredArgsConstructor
@Tag(name = "Participations", description = "Task participation management APIs")
public class ParticipationController {

    private final ParticipationService participationService;

    @Operation(summary = "Join task", description = "Add user to task participants")
    @PostMapping("/join")
    public ResponseEntity<Participation> joinTask(@RequestParam Long taskId, 
                                                 @RequestParam Long userId,
                                                 @RequestParam(required = false) String notes) {
        log.info("Request for user {} to join task {}", userId, taskId);
        
        try {
            Participation participation = participationService.joinTask(taskId, userId, notes);
            return ResponseEntity.ok(participation);
        } catch (Exception e) {
            log.error("Error joining task: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Leave task", description = "Remove user from task participants")
    @DeleteMapping("/leave")
    public ResponseEntity<Void> leaveTask(@RequestParam Long taskId, @RequestParam Long userId) {
        log.info("Request for user {} to leave task {}", userId, taskId);
        
        boolean success = participationService.leaveTask(taskId, userId);
        
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get task participants", description = "Retrieve all participants of a specific task")
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<Participation>> getTaskParticipants(@PathVariable Long taskId) {
        log.info("Request to get participants for task {}", taskId);
        
        List<Participation> participants = participationService.getTaskParticipants(taskId);
        return ResponseEntity.ok(participants);
    }

    @Operation(summary = "Get user participations", description = "Retrieve all participations of a specific user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Participation>> getUserParticipations(@PathVariable Long userId) {
        log.info("Request to get participations for user {}", userId);
        
        List<Participation> participations = participationService.getUserParticipations(userId);
        return ResponseEntity.ok(participations);
    }

    @Operation(summary = "Check participation", description = "Check if user is participating in task")
    @GetMapping("/check")
    public ResponseEntity<Boolean> isUserParticipating(@RequestParam Long taskId, @RequestParam Long userId) {
        log.info("Request to check if user {} is participating in task {}", userId, taskId);
        
        boolean isParticipating = participationService.isUserParticipating(taskId, userId);
        return ResponseEntity.ok(isParticipating);
    }
}
