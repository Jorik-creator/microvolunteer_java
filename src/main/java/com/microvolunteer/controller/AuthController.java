package com.microvolunteer.controller;

import com.microvolunteer.config.KeycloakUtils;
import com.microvolunteer.dto.response.UserResponse;
import com.microvolunteer.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентифікація", description = "API для синхронізації користувачів з Keycloak")
public class AuthController {

    private final AuthService authService;
    private final KeycloakUtils keycloakUtils;

    @PostMapping("/sync")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Синхронізація користувача з Keycloak")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Користувач успішно синхронізований"),
            @ApiResponse(responseCode = "401", description = "Неавторизований доступ")
    })
    public ResponseEntity<UserResponse> syncUser(Principal principal) {
        String keycloakId = principal.getName();
        log.info("Синхронізація користувача з Keycloak ID: {}", keycloakId);

        // Отримуємо додаткову інформацію з токена через KeycloakUtils
        String username = keycloakUtils.getCurrentUsername().orElse(null);
        String email = keycloakUtils.getCurrentUserEmail().orElse(null);

        UserResponse response = authService.syncKeycloakUser(keycloakId, username, email);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sync-full")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Повна синхронізація користувача з JWT токена")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Користувач успішно синхронізований"),
            @ApiResponse(responseCode = "401", description = "Неавторизований доступ"),
            @ApiResponse(responseCode = "400", description = "Помилка обробки токена")
    })
    public ResponseEntity<UserResponse> syncUserFull(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwtToken = authHeader.substring(7);
            log.info("Повна синхронізація користувача з JWT токена");
            
            UserResponse response = authService.syncFromJwtToken(jwtToken);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/token")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Генерація внутрішнього JWT токена")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Токен успішно згенеровано"),
            @ApiResponse(responseCode = "401", description = "Неавторизований доступ")
    })
    public ResponseEntity<Map<String, String>> generateInternalToken(Principal principal) {
        String keycloakId = principal.getName();
        log.info("Генерація внутрішнього токена для користувача: {}", keycloakId);

        String token = authService.generateInternalToken(keycloakId);

        Map<String, String> response = Map.of(
                "access_token", token,
                "token_type", "Bearer",
                "expires_in", "86400",
                "note", "Internal token for service integration"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Отримати інформацію про поточного користувача")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Інформацію отримано"),
            @ApiResponse(responseCode = "401", description = "Неавторизований доступ"),
            @ApiResponse(responseCode = "404", description = "Користувача не знайдено")
    })
    public ResponseEntity<UserResponse> getCurrentUser(Principal principal) {
        String keycloakId = principal.getName();
        log.info("Отримання інформації про користувача: {}", keycloakId);

        UserResponse response = authService.getUserByKeycloakId(keycloakId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user-info")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Отримати повну інформацію з Keycloak токена")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Інформацію отримано"),
            @ApiResponse(responseCode = "401", description = "Неавторизований доступ")
    })
    public ResponseEntity<Map<String, Object>> getKeycloakUserInfo() {
        Map<String, Object> userInfo = Map.of(
                "keycloak_id", keycloakUtils.getCurrentUserKeycloakId().orElse("unknown"),
                "username", keycloakUtils.getCurrentUsername().orElse("unknown"),
                "email", keycloakUtils.getCurrentUserEmail().orElse("unknown")
        );

        return ResponseEntity.ok(userInfo);
    }
}