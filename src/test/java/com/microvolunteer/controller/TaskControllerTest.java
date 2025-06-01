package com.microvolunteer.controller;

import com.microvolunteer.entity.Task;
import com.microvolunteer.enums.TaskStatus;
import com.microvolunteer.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@ActiveProfiles("test")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private com.microvolunteer.service.JwtService jwtService;

    @Test
    void getAllTasks_ShouldReturnTasksList() throws Exception {
        // Given
        Task task = createTestTask();
        when(taskService.getAllTasks()).thenReturn(List.of(task));

        // When & Then
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Task"));
    }

    @Test
    void getTaskById_WhenTaskExists_ShouldReturnTask() throws Exception {
        // Given
        Long taskId = 1L;
        Task task = createTestTask();
        when(taskService.getTaskById(taskId)).thenReturn(Optional.of(task));

        // When & Then
        mockMvc.perform(get("/api/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    void getTaskById_WhenTaskDoesNotExist_ShouldReturn404() throws Exception {
        // Given
        Long taskId = 1L;
        when(taskService.getTaskById(taskId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/tasks/{id}", taskId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTasksByStatus_ShouldReturnFilteredTasks() throws Exception {
        // Given
        Task task = createTestTask();
        Page<Task> taskPage = new PageImpl<>(List.of(task));
        when(taskService.getTasksByStatus(eq(TaskStatus.OPEN), any(PageRequest.class)))
                .thenReturn(taskPage);

        // When & Then
        mockMvc.perform(get("/api/tasks/status/{status}", TaskStatus.OPEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].status").value("OPEN"));
    }

    @Test
    void getTasksByCategory_ShouldReturnTasksFromCategory() throws Exception {
        // Given
        Long categoryId = 1L;
        Task task = createTestTask();
        when(taskService.getTasksByCategory(categoryId)).thenReturn(List.of(task));

        // When & Then
        mockMvc.perform(get("/api/tasks/category/{categoryId}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getTasksByCreator_ShouldReturnTasksFromCreator() throws Exception {
        // Given
        Long creatorId = 1L;
        Task task = createTestTask();
        when(taskService.getTasksByCreator(creatorId)).thenReturn(List.of(task));

        // When & Then
        mockMvc.perform(get("/api/tasks/creator/{creatorId}", creatorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    private Task createTestTask() {
        return Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
