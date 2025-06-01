package com.microvolunteer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "🏥 Health & Info", description = "Перевірка стану системи та інформація про API")
public class HealthController {

    @Operation(
        summary = "Перевірка роботи API",
        description = """
            Простий endpoint для перевірки, що API працює.
            
            **Використання:**
            - Перевірка доступності сервісу
            - Моніторинг стану системи
            - Тестування підключення
            """,
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "API працює нормально",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = """
                            {
                              "status": "UP",
                              "message": "Microvolunteer API працює",
                              "timestamp": "2024-01-15T10:30:00",
                              "version": "1.0.0"
                            }
                            """
                    )
                )
            )
        }
    )
    @GetMapping("/health")
    public ResponseEntity<HealthStatus> health() {
        return ResponseEntity.ok(HealthStatus.builder()
                .status("UP")
                .message("🤝 Microvolunteer API працює нормально!")
                .timestamp(LocalDateTime.now())
                .version("1.0.0")
                .build());
    }

    @Operation(
        summary = "Інформація про API",
        description = """
            Повертає детальну інформацію про API та його можливості.
            
            **Інформація включає:**
            - Опис платформи
            - Доступні endpoints
            - Статистику системи
            """
    )
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        return ResponseEntity.ok(Map.of(
            "name", "Microvolunteer API",
            "description", "Платформа для з'єднання волонтерів та людей, які потребують допомоги",
            "version", "1.0.0",
            "endpoints", Map.of(
                "tasks", "Управління завданнями волонтерства",
                "users", "Управління користувачами",
                "categories", "Категорії завдань",
                "participations", "Система участі у завданнях",
                "auth", "Автентифікація та авторизація"
            ),
            "features", Map.of(
                "authentication", "Інтеграція з Keycloak",
                "security", "JWT токени",
                "database", "PostgreSQL з Flyway міграціями",
                "documentation", "OpenAPI 3.0 з Swagger UI",
                "testing", "Unit та інтеграційні тести"
            ),
            "contact", Map.of(
                "email", "contact@microvolunteer.com",
                "github", "https://github.com/microvolunteer"
            ),
            "swagger_ui", "/swagger-ui.html",
            "api_docs", "/v3/api-docs",
            "timestamp", LocalDateTime.now()
        ));
    }

    @Data
    @lombok.Builder
    @Schema(description = "Статус здоров'я API")
    public static class HealthStatus {
        @Schema(description = "Статус системи", example = "UP")
        private String status;
        
        @Schema(description = "Повідомлення про стан", example = "API працює нормально")
        private String message;
        
        @Schema(description = "Час перевірки")
        private LocalDateTime timestamp;
        
        @Schema(description = "Версія API", example = "1.0.0")
        private String version;
    }
}
