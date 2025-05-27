package com.microvolunteer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microvolunteer.dto.request.TaskCreateRequest;
import com.microvolunteer.dto.response.TaskResponse;
import com.microvolunteer.enums.TaskStatus;
import com.microvolunteer.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    private TaskCreateRequest createRequest;
    private TaskResponse taskResponse;

    @BeforeEach
    void setUp() {
        createRequest = TaskCreateRequest.builder()
                .title("Потрібна допомога з покупками")
                .description("Допоможіть купити продукти")
                .categoryId(1L)
                .location("Київ")
                .startDate(LocalDateTime.now().plusDays(1))
                .maxParticipants(1)
                .build();

        taskResponse = TaskResponse.builder()
                .id(1L)
                .title("Потрібна допомога з покупками")
                .status(TaskStatus.OPEN)
                .maxParticipants(1)
                .currentParticipants(0)
                .availableSpots(1)
                .build();
    }

    @Test
    @WithMockUser
    void createTask_Success() throws Exception {
        // Given
        when(taskService.createTask(anyString(), any(TaskCreateRequest.class))).thenReturn(taskResponse);

        // When & Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Потрібна допомога з покупками"));
    }

    @Test
    void getTaskById_Success() throws Exception {
        // Given
        when(taskService.getTaskById(eq(1L), anyString())).thenReturn(taskResponse);

        // When & Then
        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Потрібна допомога з покупками"));
    }

    @Test
    void getRecentTasks_Success() throws Exception {
        // Given
        List<TaskResponse> tasks = Arrays.asList(taskResponse);
        when(taskService.getRecentTasks()).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/api/tasks/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser
    void joinTask_Success() throws Exception {
        // Given
        when(taskService.joinTask(anyString(), eq(1L))).thenReturn(taskResponse);

        // When & Then
        mockMvc.perform(post("/api/tasks/1/join"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}