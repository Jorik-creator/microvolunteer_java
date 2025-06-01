package com.microvolunteer.controller;

import com.microvolunteer.dto.request.TaskCreateRequest;
import com.microvolunteer.dto.request.TaskSearchRequest;
import com.microvolunteer.dto.response.TaskResponse;
import com.microvolunteer.entity.Category;
import com.microvolunteer.entity.Task;
import com.microvolunteer.entity.User;
import com.microvolunteer.enums.TaskStatus;
import com.microvolunteer.mapper.TaskMapper;
import com.microvolunteer.service.CategoryService;
import com.microvolunteer.service.TaskService;
import com.microvolunteer.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "📋 Tasks", description = "Управління завданнями волонтерства")
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final TaskMapper taskMapper;

    @Operation(
        summary = "Отримати всі завдання",
        description = """
            Повертає список всіх завдань у системі.
            
            **Можливі використання:**
            - Перегляд всіх доступних завдань
            - Отримання загального огляду активності платформи
            """,
        tags = {"Tasks"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Список завдань успішно отримано"),
        @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера")
    })
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        log.info("Request to get all tasks");
        List<Task> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    @Operation(
        summary = "Створити нове завдання",
        description = """
            Створює нове завдання для волонтерської діяльності.
            
            **Кроки:**
            1. Автентифікований користувач стає автором завдання
            2. Завдання автоматично отримує статус OPEN
            3. Повертається детальна інформація про створене завдання
            
            **Дозволені типи користувачів:** ORGANIZER, AFFECTED_PERSON
            """,
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Завдання успішно створено",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TaskResponse.class),
                examples = @ExampleObject(
                    name = "Приклад створеного завдання",
                    value = """
                        {
                          "id": 1,
                          "title": "Допомога з покупками",
                          "description": "Потрібна допомога з покупкою продуктів",
                          "status": "OPEN",
                          "maxParticipants": 2,
                          "createdAt": "2024-01-15T10:30:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Невірні дані запиту"),
        @ApiResponse(responseCode = "401", description = "Не автентифікований"),
        @ApiResponse(responseCode = "403", description = "Недостатньо прав")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Дані для створення завдання",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = TaskCreateRequest.class),
            examples = @ExampleObject(
                name = "Приклад запиту",
                value = """
                    {
                      "title": "Допомога з покупками",
                      "description": "Потрібна допомога з покупкою продуктів для літньої людини",
                      "location": "АТБ, вул. Хрещатик, 20",
                      "categoryId": 3,
                      "maxParticipants": 2,
                      "scheduledAt": "2024-01-20T14:00:00",
                      "requiredSkills": "Водійські права бажано"
                    }
                    """
            )
        )
    )
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskCreateRequest request, Principal principal) {
        log.info("Request to create new task: {}", request.getTitle());
        
        try {
            // Get current user by keycloak ID
            String keycloakId = principal.getName();
            User creator = userService.getUserEntityByKeycloakId(keycloakId);
            
            // Get category
            Category category = categoryService.getCategoryEntityById(request.getCategoryId());
            
            // Map request to entity
            Task task = taskMapper.toEntity(request);
            task.setCreator(creator);
            task.setCategory(category);
            
            // Create task
            Task createdTask = taskService.createTask(task);
            
            // Map to response
            TaskResponse response = taskMapper.toResponse(createdTask);
            
            return ResponseEntity.status(201).body(response);
            
        } catch (Exception e) {
            log.error("Error creating task: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
        summary = "Пошук завдань з фільтрами",
        description = """
            Розширений пошук завдань з можливістю фільтрації.
            
            **Доступні фільтри:**
            - Текстовий пошук по заголовку та опису
            - Фільтр по категорії
            - Фільтр по статусу
            - Фільтр по даті
            - Пагінація результатів
            """,
        parameters = {
            @Parameter(name = "query", description = "Текст для пошуку", example = "допомога"),
            @Parameter(name = "categoryId", description = "ID категорії", example = "1"),
            @Parameter(name = "status", description = "Статус завдання", example = "OPEN"),
            @Parameter(name = "page", description = "Номер сторінки", example = "0"),
            @Parameter(name = "size", description = "Розмір сторінки", example = "10")
        }
    )
    @GetMapping("/search")
    public ResponseEntity<Page<Task>> searchTasks(TaskSearchRequest searchRequest) {
        log.info("Request to search tasks with criteria: {}", searchRequest);
        
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());
        
        LocalDateTime dateFrom = searchRequest.getDateFrom() != null 
            ? searchRequest.getDateFrom().atStartOfDay() 
            : null;
        LocalDateTime dateTo = searchRequest.getDateTo() != null 
            ? searchRequest.getDateTo().atTime(23, 59, 59) 
            : null;
            
        Page<Task> tasks = taskService.searchTasks(
            searchRequest.getQuery(),
            searchRequest.getCategoryId(),
            searchRequest.getStatus(),
            dateFrom,
            dateTo,
            pageable
        );
        
        return ResponseEntity.ok(tasks);
    }

    @Operation(
        summary = "Отримати завдання за ID",
        description = "Повертає детальну інформацію про конкретне завдання.",
        parameters = @Parameter(name = "id", description = "Унікальний ідентифікатор завдання", example = "1")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Завдання знайдено"),
        @ApiResponse(responseCode = "404", description = "Завдання не знайдено")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        log.info("Request to get task with ID: {}", id);
        return taskService.getTaskById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Оновити завдання",
        description = """
            Оновлює існуюче завдання. Тільки автор завдання може його оновити.
            
            **Обмеження:**
            - Тільки автор може редагувати завдання
            - Не можна змінити завдання зі статусом COMPLETED
            """,
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id, 
                                                  @Valid @RequestBody TaskCreateRequest request,
                                                  Principal principal) {
        log.info("Request to update task with ID: {}", id);
        
        try {
            // Get current user
            String keycloakId = principal.getName();
            User currentUser = userService.getUserEntityByKeycloakId(keycloakId);
            
            // Check if task exists and user has permission
            Task existingTask = taskService.getTaskById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
                
            if (!existingTask.getCreator().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(403).build(); // Forbidden
            }
            
            // Get category
            Category category = categoryService.getCategoryEntityById(request.getCategoryId());
            
            // Map request to entity
            Task updatedTask = taskMapper.toEntity(request);
            updatedTask.setCategory(category);
            
            // Update task
            Task updated = taskService.updateTask(id, updatedTask)
                .orElseThrow(() -> new RuntimeException("Failed to update task"));
            
            // Map to response
            TaskResponse response = taskMapper.toResponse(updated);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error updating task: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
        summary = "Видалити завдання",
        description = "Видаляє завдання. Тільки автор може видалити своє завдання.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id, Principal principal) {
        log.info("Request to delete task with ID: {}", id);
        
        try {
            // Get current user
            String keycloakId = principal.getName();
            User currentUser = userService.getUserEntityByKeycloakId(keycloakId);
            
            // Check if task exists and user has permission
            Task existingTask = taskService.getTaskById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
                
            if (!existingTask.getCreator().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(403).build(); // Forbidden
            }
            
            boolean deleted = taskService.deleteTask(id);
            
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error deleting task: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
        summary = "Завдання за статусом",
        description = "Отримати завдання відфільтровані за статусом з пагінацією."
    )
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<Task>> getTasksByStatus(
        @Parameter(description = "Статус завдання", example = "OPEN") 
        @PathVariable TaskStatus status, 
        Pageable pageable) {
        log.info("Request to get tasks with status: {}", status);
        Page<Task> tasks = taskService.getTasksByStatus(status, pageable);
        return ResponseEntity.ok(tasks);
    }

    @Operation(
        summary = "Завдання за категорією",
        description = "Отримати всі завдання певної категорії."
    )
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Task>> getTasksByCategory(
        @Parameter(description = "ID категорії", example = "1") 
        @PathVariable Long categoryId) {
        log.info("Request to get tasks for category ID: {}", categoryId);
        List<Task> tasks = taskService.getTasksByCategory(categoryId);
        return ResponseEntity.ok(tasks);
    }

    @Operation(
        summary = "Завдання за автором",
        description = "Отримати всі завдання створені конкретним користувачем."
    )
    @GetMapping("/creator/{creatorId}")
    public ResponseEntity<List<Task>> getTasksByCreator(
        @Parameter(description = "ID автора завдання", example = "1") 
        @PathVariable Long creatorId) {
        log.info("Request to get tasks created by user ID: {}", creatorId);
        List<Task> tasks = taskService.getTasksByCreator(creatorId);
        return ResponseEntity.ok(tasks);
    }

    @Operation(
        summary = "Завершити завдання",
        description = """
            Позначити завдання як завершене.
            
            **Правила:**
            - Тільки автор завдання може його завершити
            - Завдання отримує статус COMPLETED
            - Завершене завдання не можна редагувати
            """,
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Task> completeTask(
        @Parameter(description = "ID завдання", example = "1") @PathVariable Long id, 
        @Parameter(description = "ID користувача", example = "1") @RequestParam Long userId) {
        log.info("Request to complete task {} by user {}", id, userId);
        return taskService.completeTask(id, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
