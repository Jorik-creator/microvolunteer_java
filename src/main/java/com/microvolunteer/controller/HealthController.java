package com.microvolunteer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Health check controller for monitoring application status.
 */
@RestController
@RequestMapping("/api/health")
@Tag(name = "Health Check", description = "Application health monitoring endpoints")
public class HealthController {
    
    @Operation(
        summary = "Application health check",
        description = "Check if the application is running and accessible"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application is healthy"),
        @ApiResponse(responseCode = "503", description = "Application is unhealthy")
    })
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> healthInfo = Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now(),
            "service", "MicroVolunteer Backend",
            "version", "1.0.0"
        );
        
        return ResponseEntity.ok(healthInfo);
    }
    
    @Operation(
        summary = "Simple ping endpoint",
        description = "Simple endpoint to check basic connectivity"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pong response received")
    })
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of(
            "message", "pong",
            "timestamp", LocalDateTime.now().toString()
        ));
    }
}
