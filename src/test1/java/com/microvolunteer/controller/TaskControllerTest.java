package com.microvolunteer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microvolunteer.config.TestSecurityConfig;
import com.microvolunteer.dto.request.TaskCreateRequest;
import com.microvolunteer.dto.request.TaskSearchRequest;
import com.microvolunteer.dto.response.TaskResponse;
import com.microvolunteer.enums.TaskStatus;
import com.microvolunteer.exception.BusinessException;
import com.microvolunteer.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("TaskController Integration Tests")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    private TaskCreateRequest taskCreateRequest;
    private TaskResponse taskResponse;

    @BeforeEach
    void setUp() {
        taskCreateRequest = new TaskCreateRequest();
        taskCreateRequest.setTitle("Test Task");
        taskCreateRequest.setDescription("Test Description");
        taskCreateRequest.setLocation("Test Location");
        taskCreateRequest.setCategoryIds(Set.of(1L));
        taskCreateRequest.setDeadline(LocalDateTime.now().plusDays(7));

        taskResponse = new TaskResponse();
        taskResponse.setId(1L);
        taskResponse.setTitle("Test Task");
        taskResponse.setDescription("Test Description");
        taskResponse.setLocation("Test Location");
        taskResponse.setStatus(TaskStatus.OPEN);
        taskResponse.setParticipantsCount(0);
        taskResponse.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Create Task Tests")
    class CreateTaskTests {

        @Test
        @DisplayName("Should create task successfully")
        @WithMockUser(roles = "SENSITIVE")
        void shouldCreateTaskSuccessfully() throws Exception {
            // Given
            when(taskService.createTask(any(TaskCreateRequest.class), anyString()))
                    .thenReturn(taskResponse);

            // When & Then
            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(taskCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Test Task"))
                    .andExpect(jsonPath("$.description").value("Test Description"))
                    .andExpect(jsonPath("$.status").value("OPEN"))
                    .andExpect(jsonPath("$.participantsCount").value(0));

            verify(taskService).createTask(any(TaskCreateRequest.class), anyString());
        }

        @Test
        @DisplayName("Should return 400 when request is invalid")
        @WithMockUser(roles = "SENSITIVE")
        void shouldReturn400WhenRequestInvalid() throws Exception {
            // Given
            TaskCreateRequest invalidRequest = new TaskCreateRequest();
            // Missing required fields

            // When & Then
            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(taskService, never()).createTask(any(TaskCreateRequest.class), anyString());
        }

        @Test
        @DisplayName("Should return 403 when user unauthorized")
        @WithMockUser(roles = "VOLUNTEER")
        void shouldReturn403WhenUserUnauthorized() throws Exception {
            // Given
            when(taskService.createTask(any(TaskCreateRequest.class), anyString()))
                    .thenThrow(new BusinessException("Unauthorized access", "UNAUTHORIZED_ACCESS"));

            // When & Then
            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(taskCreateRequest)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Get Task Tests")
    class GetTaskTests {

        @Test
        @DisplayName("Should get task by ID successfully")
        @WithMockUser
        void shouldGetTaskByIdSuccessfully() throws Exception {
            // Given
            when(taskService.getTaskById(1L)).thenReturn(taskResponse);

            // When & Then
            mockMvc.perform(get("/api/tasks/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Test Task"))
                    .andExpect(jsonPath("$.status").value("OPEN"));

            verify(taskService).getTaskById(1L);
        }

        @Test
        @DisplayName("Should return 404 when task not found")
        @WithMockUser
        void shouldReturn404WhenTaskNotFound() throws Exception {
            // Given
            when(taskService.getTaskById(1L))
                    .thenThrow(new BusinessException("Task not found", "TASK_NOT_FOUND"));

            // When & Then
            mockMvc.perform(get("/api/tasks/1"))
                    .andExpect(status().isNotFound());

            verify(taskService).getTaskById(1L);
        }

        @Test
        @DisplayName("Should get tasks by status successfully")
        @WithMockUser
        void shouldGetTasksByStatusSuccessfully() throws Exception {
            // Given
            List<TaskResponse> tasks = List.of(taskResponse);
            when(taskService.getTasksByStatus(TaskStatus.OPEN)).thenReturn(tasks);

            // When & Then
            mockMvc.perform(get("/api/tasks/status/OPEN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].status").value("OPEN"));

            verify(taskService).getTasksByStatus(TaskStatus.OPEN);
        }

        @Test
        @DisplayName("Should get my tasks successfully")
        @WithMockUser
        void shouldGetMyTasksSuccessfully() throws Exception {
            // Given
            List<TaskResponse> tasks = List.of(taskResponse);
            when(taskService.getTasksByAuthor(anyString())).thenReturn(tasks);

            // When & Then
            mockMvc.perform(get("/api/tasks/my"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1));

            verify(taskService).getTasksByAuthor(anyString());
        }
    }

    @Nested
    @DisplayName("Search Tasks Tests")
    class SearchTasksTests {

        @Test
        @DisplayName("Should search tasks with default parameters")
        @WithMockUser
        void shouldSearchTasksWithDefaultParameters() throws Exception {
            // Given
            Page<TaskResponse> tasksPage = new PageImpl<>(List.of(taskResponse), PageRequest.of(0, 20), 1);
            when(taskService.searchTasks(any(TaskSearchRequest.class))).thenReturn(tasksPage);

            // When & Then
            mockMvc.perform(get("/api/tasks"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1));

            verify(taskService).searchTasks(any(TaskSearchRequest.class));
        }

        @Test
        @DisplayName("Should search tasks with custom parameters")
        @WithMockUser
        void shouldSearchTasksWithCustomParameters() throws Exception {
            // Given
            Page<TaskResponse> tasksPage = new PageImpl<>(List.of(taskResponse), PageRequest.of(0, 10), 1);
            when(taskService.searchTasks(any(TaskSearchRequest.class))).thenReturn(tasksPage);

            // When & Then
            mockMvc.perform(get("/api/tasks")
                            .param("page", "0")
                            .param("size", "10")
                            .param("sortBy", "createdAt")
                            .param("sortDirection", "desc")
                            .param("status", "OPEN")
                            .param("searchText", "test"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(1));

            verify(taskService).searchTasks(any(TaskSearchRequest.class));
        }
    }

    @Nested
    @DisplayName("Complete Task Tests")
    class CompleteTaskTests {

        @Test
        @DisplayName("Should complete task successfully")
        @WithMockUser(roles = "SENSITIVE")
        void shouldCompleteTaskSuccessfully() throws Exception {
            // Given
            TaskResponse completedTask = new TaskResponse();
            completedTask.setId(1L);
            completedTask.setStatus(TaskStatus.COMPLETED);
            when(taskService.completeTask(1L, anyString())).thenReturn(completedTask);

            // When & Then
            mockMvc.perform(patch("/api/tasks/1/complete"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.status").value("COMPLETED"));

            verify(taskService).completeTask(1L, anyString());
        }

        @Test
        @DisplayName("Should return 403 when user not authorized to complete")
        @WithMockUser(roles = "SENSITIVE")
        void shouldReturn403WhenUserNotAuthorizedToComplete() throws Exception {
            // Given
            when(taskService.completeTask(1L, anyString()))
                    .thenThrow(new BusinessException("Unauthorized access", "UNAUTHORIZED_ACCESS"));

            // When & Then
            mockMvc.perform(patch("/api/tasks/1/complete"))
                    .andExpect(status().isForbidden());

            verify(taskService).completeTask(1L, anyString());
        }

        @Test
        @DisplayName("Should return 409 when task already completed")
        @WithMockUser(roles = "SENSITIVE")
        void shouldReturn409WhenTaskAlreadyCompleted() throws Exception {
            // Given
            when(taskService.completeTask(1L, anyString()))
                    .thenThrow(new BusinessException("Invalid task status", "INVALID_TASK_STATUS"));

            // When & Then
            mockMvc.perform(patch("/api/tasks/1/complete"))
                    .andExpect(status().isConflict());

            verify(taskService).completeTask(1L, anyString());
        }
    }

    @Nested
    @DisplayName("Delete Task Tests")
    class DeleteTaskTests {

        @Test
        @DisplayName("Should delete task successfully")
        @WithMockUser(roles = "SENSITIVE")
        void shouldDeleteTaskSuccessfully() throws Exception {
            // Given
            doNothing().when(taskService).deleteTask(1L, anyString());

            // When & Then
            mockMvc.perform(delete("/api/tasks/1"))
                    .andExpect(status().isNoContent());

            verify(taskService).deleteTask(1L, anyString());
        }

        @Test
        @DisplayName("Should return 403 when user not authorized to delete")
        @WithMockUser(roles = "VOLUNTEER")
        void shouldReturn403WhenUserNotAuthorizedToDelete() throws Exception {
            // Given
            doThrow(new BusinessException("Unauthorized access", "UNAUTHORIZED_ACCESS"))
                    .when(taskService).deleteTask(1L, anyString());

            // When & Then
            mockMvc.perform(delete("/api/tasks/1"))
                    .andExpect(status().isForbidden());

            verify(taskService).deleteTask(1L, anyString());
        }

        @Test
        @DisplayName("Should return 409 when task has participants")
        @WithMockUser(roles = "SENSITIVE")
        void shouldReturn409WhenTaskHasParticipants() throws Exception {
            // Given
            doThrow(new BusinessException("Task has participants", "TASK_HAS_PARTICIPANTS"))
                    .when(taskService).deleteTask(1L, anyString());

            // When & Then
            mockMvc.perform(delete("/api/tasks/1"))
                    .andExpect(status().isConflict());

            verify(taskService).deleteTask(1L, anyString());
        }
    }

    @Nested
    @DisplayName("Additional Endpoints Tests")
    class AdditionalEndpointsTests {

        @Test
        @DisplayName("Should get recent tasks successfully")
        @WithMockUser
        void shouldGetRecentTasksSuccessfully() throws Exception {
            // Given
            List<TaskResponse> tasks = List.of(taskResponse);
            when(taskService.getRecentTasks(7)).thenReturn(tasks);

            // When & Then
            mockMvc.perform(get("/api/tasks/recent")
                            .param("days", "7"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1));

            verify(taskService).getRecentTasks(7);
        }

        @Test
        @DisplayName("Should get tasks with approaching deadline successfully")
        @WithMockUser
        void shouldGetTasksWithApproachingDeadlineSuccessfully() throws Exception {
            // Given
            List<TaskResponse> tasks = List.of(taskResponse);
            when(taskService.getTasksWithApproachingDeadline(24)).thenReturn(tasks);

            // When & Then
            mockMvc.perform(get("/api/tasks/deadline-approaching")
                            .param("hours", "24"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1));

            verify(taskService).getTasksWithApproachingDeadline(24);
        }

        @Test
        @DisplayName("Should get task statistics successfully")
        @WithMockUser
        void shouldGetTaskStatisticsSuccessfully() throws Exception {
            // Given
            TaskService.TaskStatistics statistics = new TaskService.TaskStatistics(10, 3, 4, 3);
            when(taskService.getTaskStatistics()).thenReturn(statistics);

            // When & Then
            mockMvc.perform(get("/api/tasks/statistics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(10))
                    .andExpect(jsonPath("$.open").value(3))
                    .andExpect(jsonPath("$.inProgress").value(4))
                    .andExpect(jsonPath("$.completed").value(3));

            verify(taskService).getTaskStatistics();
        }
    }
}
