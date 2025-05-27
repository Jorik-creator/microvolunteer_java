package com.microvolunteer.controller;

import com.microvolunteer.dto.request.TaskCreateRequest;
import com.microvolunteer.dto.request.TaskSearchRequest;
import com.microvolunteer.dto.response.TaskResponse;
import com.microvolunteer.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Завдання", description = "API для роботи із завданнями")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @RolesAllowed("user")
    @Operation(summary = "Створити нове завдання")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Завдання успішно створено"),
            @ApiResponse(responseCode = "400", description = "Невалідні дані"),
            @ApiResponse(responseCode = "401", description = "Неавторизований доступ"),
            @ApiResponse(responseCode = "403", description = "Недостатньо прав")
    })
    public ResponseEntity<TaskResponse> createTask(
            Principal principal,
            @Valid @RequestBody TaskCreateRequest request) {
        String keycloakId = principal.getName();
        log.info("Створення нового завдання користувачем: {}", keycloakId);
        TaskResponse response = taskService.createTask(keycloakId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/list")
    @Operation(summary = "Отримати список завдань")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список завдань отримано")
    })
    public ResponseEntity<Page<TaskResponse>> getTasks(
            @ModelAttribute TaskSearchRequest searchRequest,
            Principal principal) {
        String keycloakId = principal != null ? principal.getName() : null;
        log.info("Отримання списку завдань з параметрами: {}", searchRequest);
        Page<TaskResponse> tasks = taskService.searchTasks(searchRequest, keycloakId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Отримати завдання за ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Завдання знайдено"),
            @ApiResponse(responseCode = "404", description = "Завдання не знайдено")
    })
    public ResponseEntity<TaskResponse> getTaskById(
            @Parameter(description = "ID завдання") @PathVariable Long id,
            Principal principal) {
        String keycloakId = principal != null ? principal.getName() : null;
        log.info("Отримання завдання з ID: {}", id);
        TaskResponse response = taskService.getTaskById(id, keycloakId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @RolesAllowed("user")
    @Operation(summary = "Оновити завдання")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Завдання успішно оновлено"),
            @ApiResponse(responseCode = "400", description = "Невалідні дані"),
            @ApiResponse(responseCode = "401", description = "Неавторизований доступ"),
            @ApiResponse(responseCode = "403", description = "Недостатньо прав"),
            @ApiResponse(responseCode = "404", description = "Завдання не знайдено")
    })
    public ResponseEntity<TaskResponse> updateTask(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody TaskCreateRequest request) {
        String keycloakId = principal.getName();
        log.info("Оновлення завдання {} користувачем: {}", id, keycloakId);
        TaskResponse response = taskService.updateTask(keycloakId, id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/join")
    @RolesAllowed("user")
    @Operation(summary = "Приєднатися до завдання")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успішно приєднано до завдання"),
            @ApiResponse(responseCode = "400", description = "Неможливо приєднатися"),
            @ApiResponse(responseCode = "401", description = "Неавторизований доступ"),
            @ApiResponse(responseCode = "404", description = "Завдання не знайдено")
    })
    public ResponseEntity<TaskResponse> joinTask(
            Principal principal,
            @PathVariable Long id) {
        String keycloakId = principal.getName();
        log.info("Приєднання до завдання {} користувачем: {}", id, keycloakId);
        TaskResponse response = taskService.joinTask(keycloakId, id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/leave")
    @RolesAllowed("user")
    @Operation(summary = "Відмовитися від участі у завданні")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успішно відмовлено від участі"),
            @ApiResponse(responseCode = "400", description = "Неможливо відмовитися"),
            @ApiResponse(responseCode = "401", description = "Неавторизований доступ"),
            @ApiResponse(responseCode = "404", description = "Завдання не знайдено")
    })
    public ResponseEntity<TaskResponse> leaveTask(
            Principal principal,
            @PathVariable Long id) {
        String keycloakId = principal.getName();
        log.info("Відмова від участі у завданні {} користувачем: {}", id, keycloakId);
        TaskResponse response = taskService.leaveTask(keycloakId, id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/complete")
    @RolesAllowed("user")
    @Operation(summary = "Позначити завдання як завершене")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Завдання успішно завершено"),
            @ApiResponse(responseCode = "400", description = "Неможливо завершити"),
            @ApiResponse(responseCode = "401", description = "Неавторизований доступ"),
            @ApiResponse(responseCode = "403", description = "Недостатньо прав"),
            @ApiResponse(responseCode = "404", description = "Завдання не знайдено")
    })
    public ResponseEntity<TaskResponse> completeTask(
            Principal principal,
            @PathVariable Long id) {
        String keycloakId = principal.getName();
        log.info("Завершення завдання {} користувачем: {}", id, keycloakId);
        TaskResponse response = taskService.completeTask(keycloakId, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recent")
    @Operation(summary = "Отримати останні завдання")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список останніх завдань")
    })
    public ResponseEntity<List<TaskResponse>> getRecentTasks() {
        log.info("Отримання останніх завдань");
        List<TaskResponse> tasks = taskService.getRecentTasks();
        return ResponseEntity.ok(tasks);
    }
}