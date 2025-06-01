package com.microvolunteer.controller;

import com.microvolunteer.entity.Participation;
import com.microvolunteer.service.ParticipationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ParticipationController.class)
class ParticipationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ParticipationService participationService;

    @Test
    @WithMockUser(roles = "USER")
    void joinTask_ShouldReturnOk() throws Exception {
        // Given
        Participation participation = new Participation();
        participation.setId(1L);
        participation.setJoinedAt(LocalDateTime.now());

        when(participationService.joinTask(anyLong(), anyLong(), anyString())).thenReturn(participation);

        // When & Then
        mockMvc.perform(post("/api/participations/join")
                .param("taskId", "1")
                .param("userId", "1")
                .param("notes", "test notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void leaveTask_ShouldReturnOk() throws Exception {
        // Given
        when(participationService.leaveTask(anyLong(), anyLong())).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/participations/leave")
                .param("taskId", "1")
                .param("userId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void leaveTask_NotParticipating_ShouldReturnNotFound() throws Exception {
        // Given
        when(participationService.leaveTask(anyLong(), anyLong())).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/participations/leave")
                .param("taskId", "1")
                .param("userId", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserParticipations_ShouldReturnParticipationList() throws Exception {
        // Given
        Participation participation1 = new Participation();
        participation1.setId(1L);
        participation1.setJoinedAt(LocalDateTime.now());

        Participation participation2 = new Participation();
        participation2.setId(2L);
        participation2.setJoinedAt(LocalDateTime.now().minusDays(1));

        List<Participation> participations = Arrays.asList(participation1, participation2);
        when(participationService.getUserParticipations(1L)).thenReturn(participations);

        // When & Then
        mockMvc.perform(get("/api/participations/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void getTaskParticipants_ShouldReturnParticipationList() throws Exception {
        // Given
        Participation participation = new Participation();
        participation.setId(1L);
        participation.setJoinedAt(LocalDateTime.now());

        List<Participation> participations = Arrays.asList(participation);
        when(participationService.getTaskParticipants(1L)).thenReturn(participations);

        // When & Then
        mockMvc.perform(get("/api/participations/task/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void checkParticipation_ShouldReturnBoolean() throws Exception {
        // Given
        when(participationService.isUserParticipating(1L, 1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/participations/check")
                .param("taskId", "1")
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
