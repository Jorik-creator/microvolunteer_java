package com.microvolunteer.controller;

import com.microvolunteer.dto.request.UserRegistrationRequest;
import com.microvolunteer.dto.response.UserResponse;
import com.microvolunteer.enums.UserType;
import com.microvolunteer.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for user operations.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "Operations related to user management")
@SecurityRequirement(name = "keycloak")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @Operation(
        summary = "Register a new user",
        description = "Register a new user profile after successful Keycloak authentication"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User successfully registered"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "User already exists"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(
            @Valid @RequestBody UserRegistrationRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        
        String keycloakSubject = jwt.getSubject();
        UserResponse response = userService.registerUser(request, keycloakSubject);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(
        summary = "Get current user profile",
        description = "Get the profile of the currently authenticated user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User profile retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        String keycloakSubject = jwt.getSubject();
        UserResponse response = userService.getUserByKeycloakSubject(keycloakSubject);
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Update user profile",
        description = "Update the profile of the currently authenticated user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User profile updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "409", description = "Email already exists"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @Valid @RequestBody UserRegistrationRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        
        String keycloakSubject = jwt.getSubject();
        UserResponse response = userService.updateUser(keycloakSubject, request);
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Get user by ID",
        description = "Get a user profile by their ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User profile retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "User ID") @PathVariable Long id) {
        
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Get volunteers",
        description = "Get a list of all active volunteers"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Volunteers retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/volunteers")
    public ResponseEntity<List<UserResponse>> getVolunteers() {
        List<UserResponse> volunteers = userService.getVolunteers();
        return ResponseEntity.ok(volunteers);
    }
    
    @Operation(
        summary = "Get users by type",
        description = "Get users filtered by their type"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid user type"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/type/{userType}")
    public ResponseEntity<List<UserResponse>> getUsersByType(
            @Parameter(description = "User type") @PathVariable UserType userType) {
        
        List<UserResponse> users = userService.getUsersByType(userType);
        return ResponseEntity.ok(users);
    }
    
    @Operation(
        summary = "Deactivate user account",
        description = "Deactivate the currently authenticated user's account"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User account deactivated successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/me")
    public ResponseEntity<Void> deactivateCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        String keycloakSubject = jwt.getSubject();
        userService.deactivateUser(keycloakSubject);
        
        return ResponseEntity.noContent().build();
    }
}
