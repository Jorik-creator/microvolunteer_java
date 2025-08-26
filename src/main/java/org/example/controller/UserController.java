package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.UserResponse;
import org.example.dto.UserStatisticsResponse;
import org.example.dto.UserUpdateRequest;
import org.example.model.UserType;
import org.example.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get all users", description = "Get paginated list of all users with optional filters")  
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) UserType userType,
            @RequestParam(required = false) Boolean isActive,
            Pageable pageable) {
        Page<UserResponse> users = userService.getAllUsers(username, email, userType, isActive, pageable);
        return ResponseEntity.ok(users);
    }
    
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID", description = "Get user details by ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/profile")
    @Operation(summary = "Get current user profile", description = "Get current authenticated user's profile")
    public ResponseEntity<UserResponse> getCurrentUserProfile(Authentication authentication) {
        UserResponse user = userService.getUserByUsername(authentication.getName());
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    @Operation(summary = "Update current user profile", description = "Update current authenticated user's profile")
    public ResponseEntity<UserResponse> updateCurrentUserProfile(
            @Valid @RequestBody UserUpdateRequest request,
            Authentication authentication) {
        UserResponse updatedUser = userService.updateProfile(authentication.getName(), request);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user", description = "Update user by ID (Admin only)")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user", description = "Soft delete user by ID (Admin only)")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get user statistics", description = "Get current user's statistics")
    public ResponseEntity<UserStatisticsResponse> getCurrentUserStatistics(Authentication authentication) {
        UserStatisticsResponse statistics = userService.getUserStatistics(authentication.getName());
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user statistics by ID", description = "Get user statistics by ID (Admin only)")
    public ResponseEntity<UserStatisticsResponse> getUserStatistics(@PathVariable Long id) {
        UserStatisticsResponse statistics = userService.getUserStatisticsById(id);
        return ResponseEntity.ok(statistics);
    }
}