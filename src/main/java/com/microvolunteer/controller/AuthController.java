package com.microvolunteer.controller;

import com.microvolunteer.config.KeycloakUtils;
import com.microvolunteer.dto.request.UserRegistrationRequest;
import com.microvolunteer.dto.response.UserResponse;
import com.microvolunteer.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентифікація", description = "API для реєстрації та синхронізації користувачів")
public class AuthController {

    private final AuthService authService;
    private final KeycloakUtils keycloakUtils;

    @PostMapping("/register")
    @Operation(summary = "Реєстрація нового користувача")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Користувач успішно зареєстрований"),
            @ApiResponse(responseCode = "400", description = "Невалідні дані"),
            @ApiResponse(responseCode = "409", description = "Користувач вже існує")
    })
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
        log.info("Реєстрація нового користувача: {}", request.getUsername());
        UserResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/sync")
    @RolesAllowed("user")
    @Operation(summary = "Синхронізація користувача з Keycloak")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Користувач успішно синхронізований"),
            @ApiResponse(responseCode = "401", description = "Неавторизований доступ")
    })
    public ResponseEntity<UserResponse> syncUser(Principal principal) {
        String keycloakId = principal.getName();
        log.info("Синхронізація користувача з Keycloak ID: {}", keycloakId);

        String username = keycloakUtils.getCurrentUsername().orElse(null);
        String email = keycloakUtils.getCurrentUserEmail().orElse(null);

        UserResponse response = authService.syncKeycloakUser(keycloakId, username, email);
        return ResponseEntity.ok(response);
    }
}