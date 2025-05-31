package com.microvolunteer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microvolunteer.dto.request.TaskCreateRequest;
import com.microvolunteer.dto.request.TaskSearchRequest;
import com.microvolunteer.dto.response.TaskResponse;
import com.microvolunteer.enums.TaskStatus;
import com.microvolunteer.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@DisplayName("Тести для TaskController (Keycloak версія)")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    private TaskCreateRequest createRequest;
    private TaskResponse taskResponse;
    private final String testKeycloakId = "test-keycloak-id";

    @BeforeEach
    void setUp() {
        createRequest = TaskCreateRequest.builder()
                .title("Потрібна допомога з покупками")
                .description("Допоможіть купити продукти")
                .categoryId(1L)
                .location("Київ")
                .startDate(LocalDateTime.now().plusDays(1))
                .maxVolunteers(1)
                .requiredSkills("Комунікабельність")
                .build();

        taskResponse = TaskResponse.builder()
                .id(1L)
                .title("Потрібна допомога з покупками")
                .description("Допоможіть купити продукти")
                .status(TaskStatus.OPEN.name())
                .maxVolunteers(1)
                .currentVolunteers(0)
                .availableSpots(1)
                .location("Київ")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(username = "test-keycloak-id", roles = {"USER"})
    @DisplayName("Створення завдання - успішно")
    void createTask_Success() throws Exception {
        // Given
        when(taskService.createTask(eq(testKeycloakId), any(TaskCreateRequest.class)))
                .thenReturn(taskResponse);

        // When & Then
        mockMvc.perform(post("/api/tasks")
                        .with(user(testKeycloakId).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Потрібна допомога з покупками"))
                .andExpect(jsonPath("$.description").value("Допоможіть купити продукти"))
                .andExpect(jsonPath("$.maxVolunteers").value(1))
                .andExpect(jsonPath("$.availableSpots").value(1));
    }

    @Test
    @DisplayName("Створення завдання - без автентифікації")
    void createTask_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Створення завдання - невалідні дані")
    void createTask_InvalidData() throws Exception {
        // Given
        createRequest.setTitle(""); // Порожній title

        // When & Then
        mockMvc.perform(post("/api/tasks")
                        .with(user(testKeycloakId).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Отримання завдання за ID - успішно")
    void getTaskById_Success() throws Exception {
        // Given
        when(taskService.getTaskById(eq(1L), eq(testKeycloakId)))
                .thenReturn(taskResponse);

        // When & Then
        mockMvc.perform(get("/api/tasks/1")
                        .with(user(testKeycloakId).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Потрібна допомога з покупками"));
    }

    @Test
    @DisplayName("Отримання завдання за ID - без автентифікації")
    void getTaskById_WithoutAuth() throws Exception {
        // Given
        when(taskService.getTaskById(eq(1L), isNull())).thenReturn(taskResponse);

        // When & Then
        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("Отримання списку завдань - успішно")
    void getTasks_Success() throws Exception {
        // Given
        List<TaskResponse> tasks = Arrays.asList(taskResponse);
        Page<TaskResponse> taskPage = new PageImpl<>(tasks);
        
        when(taskService.searchTasks(any(TaskSearchRequest.class), eq(testKeycloakId)))
                .thenReturn(taskPage);

        // When & Then
        mockMvc.perform(get("/api/tasks/list")
                        .param("title", "покупки")
                        .param("page", "0")
                        .param("size", "10")
                        .with(user(testKeycloakId).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Потрібна допомога з покупками"));
    }

    @Test
    @DisplayName("Отримання останніх завдань - успішно")
    void getRecentTasks_Success() throws Exception {
        // Given
        List<TaskResponse> tasks = Arrays.asList(taskResponse);
        when(taskService.getRecentTasks()).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/api/tasks/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Потрібна допомога з покупками"));
    }

    @Test
    @WithMockUser(username = "test-keycloak-id", roles = {"USER"})
    @DisplayName("Приєднання до завдання - успішно")
    void joinTask_Success() throws Exception {
        // Given
        TaskResponse joinedTask = TaskResponse.builder()
                .id(1L)
                .title("Потрібна допомога з покупками")
                .currentVolunteers(1)
                .maxVolunteers(1)
                .availableSpots(0)
                .build();

        when(taskService.joinTask(eq(testKeycloakId), eq(1L))).thenReturn(joinedTask);

        // When & Then
        mockMvc.perform(post("/api/tasks/1/join")
                        .with(user(testKeycloakId).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.currentVolunteers").value(1))
                .andExpect(jsonPath("$.availableSpots").value(0));
    }

    @Test
    @DisplayName("Приєднання до завдання - без автентифікації")
    void joinTask_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/tasks/1/join"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test-keycloak-id", roles = {"USER"})
    @DisplayName("Відмова від участі у завданні - успішно")
    void leaveTask_Success() throws Exception {
        // Given
        TaskResponse leftTask = TaskResponse.builder()
                .id(1L)
                .title("Потрібна допомога з покупками")
                .currentVolunteers(0)
                .maxVolunteers(1)
                .availableSpots(1)
                .build();

        when(taskService.leaveTask(eq(testKeycloakId), eq(1L))).thenReturn(leftTask);

        // When & Then
        mockMvc.perform(post("/api/tasks/1/leave")
                        .with(user(testKeycloakId).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.currentVolunteers").value(0))
                .andExpect(jsonPath("$.availableSpots").value(1));
    }

    @Test
    @WithMockUser(username = "test-keycloak-id", roles = {"USER"})
    @DisplayName("Завершення завдання - успішно")
    void completeTask_Success() throws Exception {
        // Given
        TaskResponse completedTask = TaskResponse.builder()
                .id(1L)
                .title("Потрібна допомога з покупками")
                .status(TaskStatus.COMPLETED.name())
                .build();

        when(taskService.completeTask(eq(testKeycloakId), eq(1L))).thenReturn(completedTask);

        // When & Then
        mockMvc.perform(post("/api/tasks/1/complete")
                        .with(user(testKeycloakId).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(username = "test-keycloak-id", roles = {"USER"})
    @DisplayName("Оновлення завдання - успішно")
    void updateTask_Success() throws Exception {
        // Given
        TaskResponse updatedTask = TaskResponse.builder()
                .id(1L)
                .title("Оновлена назва завдання")
                .description("Оновлений опис")
                .build();

        when(taskService.updateTask(eq(testKeycloakId), eq(1L), any(TaskCreateRequest.class)))
                .thenReturn(updatedTask);

        createRequest.setTitle("Оновлена назва завдання");
        createRequest.setDescription("Оновлений опис");

        // When & Then
        mockMvc.perform(put("/api/tasks/1")
                        .with(user(testKeycloakId).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Оновлена назва завдання"))
                .andExpect(jsonPath("$.description").value("Оновлений опис"));
    }

    @Test
    @DisplayName("Всі захищені endpoints - без автентифікації")
    void protectedEndpoints_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/tasks")).andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/tasks/1")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/tasks/1/join")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/tasks/1/leave")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/tasks/1/complete")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test-keycloak-id", roles = {"WRONG_ROLE"})
    @DisplayName("Захищені endpoints з неправильною роллю - заборонено")
    void protectedEndpoints_WrongRole() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .with(user(testKeycloakId).roles("WRONG_ROLE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }
}