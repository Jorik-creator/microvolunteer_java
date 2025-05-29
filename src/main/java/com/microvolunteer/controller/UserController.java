package com.microvolunteer.controller;

import com.microvolunteer.dto.response.UserResponse;
import com.microvolunteer.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Користувачі", description = "API для роботи з користувачами")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Отримати профіль поточного користувача")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Профіль успішно отримано"),
            @ApiResponse(responseCode = "401", description = "Неавторизований доступ"),
            @ApiResponse(responseCode = "404", description = "Користувача не знайдено")
    })
    public ResponseEntity<UserResponse> getCurrentUserProfile(Principal principal) {
        String keycloakId = principal.getName();
        log.info("Отримання профілю для користувача: {}", keycloakId);
        UserResponse response = userService.getUserByKeycloakId(keycloakId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Отримати користувача за ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Користувача знайдено"),
            @ApiResponse(responseCode = "404", description = "Користувача не знайдено")
    })
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "ID користувача") @PathVariable Long id) {
        log.info("Отримання користувача з ID: {}", id);
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/statistics")
    @Operation(summary = "Отримати статистику користувача")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статистику отримано"),
            @ApiResponse(responseCode = "404", description = "Користувача не знайдено")
    })
    public ResponseEntity<Map<String, Object>> getUserStatistics(
            @Parameter(description = "ID користувача") @PathVariable Long id) {
        log.info("Отримання статистики для користувача: {}", id);
        Map<String, Object> statistics = userService.getUserStatistics(id);
        return ResponseEntity.ok(statistics);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Оновити профіль поточного користувача")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Профіль успішно оновлено"),
            @ApiResponse(responseCode = "401", description = "Неавторизований доступ"),
            @ApiResponse(responseCode = "404", description = "Користувача не знайдено")
    })
    public ResponseEntity<UserResponse> updateProfile(
            Principal principal,
            @RequestBody Map<String, String> updates) {
        String keycloakId = principal.getName();
        log.info("Оновлення профілю для користувача: {}", keycloakId);
        UserResponse response = userService.updateProfile(keycloakId, updates);
        return ResponseEntity.ok(response);
    }
}