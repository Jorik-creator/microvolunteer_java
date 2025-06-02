package com.microvolunteer.controller;

import com.microvolunteer.config.TestSecurityConfig;
import com.microvolunteer.dto.response.UserResponse;
import com.microvolunteer.enums.UserType;
import com.microvolunteer.exception.BusinessException;
import com.microvolunteer.service.ParticipationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ParticipationController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("ParticipationController Integration Tests")
class ParticipationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ParticipationService participationService;

    private UserResponse volunteerResponse;

    @BeforeEach
    void setUp() {
        volunteerResponse = new UserResponse();
        volunteerResponse.setId(1L);
        volunteerResponse.setFirstName("John");
        volunteerResponse.setLastName("Volunteer");
        volunteerResponse.setEmail("volunteer@test.com");
        volunteerResponse.setUserType(UserType.VOLUNTEER);
        volunteerResponse.setActive(true);
    }

    @Nested
    @DisplayName("Join Task Tests")
    class JoinTaskTests {

        @Test
        @DisplayName("Should join task successfully")
        @WithMockUser(roles = "VOLUNTEER")
        void shouldJoinTaskSuccessfully() throws Exception {
            // Given
            doNothing().when(participationService).joinTask(eq(1L), anyString());

            // When & Then
            mockMvc.perform(post("/api/tasks/1/participate"))
                    .andExpect(status().isNoContent());

            verify(participationService).joinTask(eq(1L), anyString());
        }

        @Test
        @DisplayName("Should return 403 when user is not volunteer")
        @WithMockUser(roles = "SENSITIVE")
        void shouldReturn403WhenUserNotVolunteer() throws Exception {
            // Given
            doThrow(new BusinessException("Unauthorized access", "UNAUTHORIZED_ACCESS"))
                    .when(participationService).joinTask(eq(1L), anyString());

            // When & Then
            mockMvc.perform(post("/api/tasks/1/participate"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 404 when task not found")
        @WithMockUser(roles = "VOLUNTEER")
        void shouldReturn404WhenTaskNotFound() throws Exception {
            // Given
            doThrow(new BusinessException("Task not found", "TASK_NOT_FOUND"))
                    .when(participationService).joinTask(eq(1L), anyString());

            // When & Then
            mockMvc.perform(post("/api/tasks/1/participate"))
                    .andExpect(status().isNotFound());

            verify(participationService).joinTask(eq(1L), anyString());
        }

        @Test
        @DisplayName("Should return 409 when already participating")
        @WithMockUser(roles = "VOLUNTEER")
        void shouldReturn409WhenAlreadyParticipating() throws Exception {
            // Given
            doThrow(new BusinessException("Already participating", "ALREADY_PARTICIPATING"))
                    .when(participationService).joinTask(eq(1L), anyString());

            // When & Then
            mockMvc.perform(post("/api/tasks/1/participate"))
                    .andExpect(status().isConflict());

            verify(participationService).joinTask(eq(1L), anyString());
        }

        @Test
        @DisplayName("Should return 409 when task not open")
        @WithMockUser(roles = "VOLUNTEER")
        void shouldReturn409WhenTaskNotOpen() throws Exception {
            // Given
            doThrow(new BusinessException("Task not open", "TASK_NOT_OPEN"))
                    .when(participationService).joinTask(eq(1L), anyString());

            // When & Then
            mockMvc.perform(post("/api/tasks/1/participate"))
                    .andExpect(status().isConflict());

            verify(participationService).joinTask(eq(1L), anyString());
        }

        @Test
        @DisplayName("Should return 409 when cannot participate in own task")
        @WithMockUser(roles = "VOLUNTEER")
        void shouldReturn409WhenCannotParticipateInOwnTask() throws Exception {
            // Given
            doThrow(new BusinessException("Cannot participate in own task", "CANNOT_PARTICIPATE_IN_OWN_TASK"))
                    .when(participationService).joinTask(eq(1L), anyString());

            // When & Then
            mockMvc.perform(post("/api/tasks/1/participate"))
                    .andExpect(status().isConflict());

            verify(participationService).joinTask(eq(1L), anyString());
        }

        @Test
        @DisplayName("Should require authentication")
        void shouldRequireAuthentication() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/tasks/1/participate"))
                    .andExpect(status().isUnauthorized());

            verify(participationService, never()).joinTask(anyLong(), anyString());
        }
    }

    @Nested
    @DisplayName("Leave Task Tests")
    class LeaveTaskTests {

        @Test
        @DisplayName("Should leave task successfully")
        @WithMockUser(roles = "VOLUNTEER")
        void shouldLeaveTaskSuccessfully() throws Exception {
            // Given
            doNothing().when(participationService).leaveTask(eq(1L), anyString());

            // When & Then
            mockMvc.perform(delete("/api/tasks/1/participate"))
                    .andExpect(status().isNoContent());

            verify(participationService).leaveTask(eq(1L), anyString());
        }

        @Test
        @DisplayName("Should return 403 when user is not volunteer")
        @WithMockUser(roles = "SENSITIVE")
        void shouldReturn403WhenUserNotVolunteer() throws Exception {
            // Given
            doThrow(new BusinessException("Unauthorized access", "UNAUTHORIZED_ACCESS"))
                    .when(participationService).leaveTask(eq(1L), anyString());

            // When & Then
            mockMvc.perform(delete("/api/tasks/1/participate"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 404 when not participating")
        @WithMockUser(roles = "VOLUNTEER")
        void shouldReturn404WhenNotParticipating() throws Exception {
            // Given
            doThrow(new BusinessException("Not participating", "NOT_PARTICIPATING"))
                    .when(participationService).leaveTask(eq(1L), anyString());

            // When & Then
            mockMvc.perform(delete("/api/tasks/1/participate"))
                    .andExpect(status().isNotFound());

            verify(participationService).leaveTask(eq(1L), anyString());
        }

        @Test
        @DisplayName("Should return 404 when task not found")
        @WithMockUser(roles = "VOLUNTEER")
        void shouldReturn404WhenTaskNotFound() throws Exception {
            // Given
            doThrow(new BusinessException("Task not found", "TASK_NOT_FOUND"))
                    .when(participationService).leaveTask(eq(1L), anyString());

            // When & Then
            mockMvc.perform(delete("/api/tasks/1/participate"))
                    .andExpect(status().isNotFound());

            verify(participationService).leaveTask(eq(1L), anyString());
        }

        @Test
        @DisplayName("Should require authentication")
        void shouldRequireAuthentication() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/tasks/1/participate"))
                    .andExpect(status().isUnauthorized());

            verify(participationService, never()).leaveTask(anyLong(), anyString());
        }
    }

    @Nested
    @DisplayName("Get Task Volunteers Tests")
    class GetTaskVolunteersTests {

        @Test
        @DisplayName("Should get task volunteers successfully")
        @WithMockUser
        void shouldGetTaskVolunteersSuccessfully() throws Exception {
            // Given
            List<UserResponse> volunteers = List.of(volunteerResponse);
            when(participationService.getTaskVolunteers(1L)).thenReturn(volunteers);

            // When & Then
            mockMvc.perform(get("/api/tasks/1/volunteers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].firstName").value("John"))
                    .andExpect(jsonPath("$[0].lastName").value("Volunteer"))
                    .andExpect(jsonPath("$[0].userType").value("VOLUNTEER"));

            verify(participationService).getTaskVolunteers(1L);
        }

        @Test
        @DisplayName("Should return empty list when no volunteers")
        @WithMockUser
        void shouldReturnEmptyListWhenNoVolunteers() throws Exception {
            // Given
            when(participationService.getTaskVolunteers(1L)).thenReturn(List.of());

            // When & Then
            mockMvc.perform(get("/api/tasks/1/volunteers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());

            verify(participationService).getTaskVolunteers(1L);
        }

        @Test
        @DisplayName("Should return 404 when task not found")
        @WithMockUser
        void shouldReturn404WhenTaskNotFound() throws Exception {
            // Given
            when(participationService.getTaskVolunteers(1L))
                    .thenThrow(new BusinessException("Task not found", "TASK_NOT_FOUND"));

            // When & Then
            mockMvc.perform(get("/api/tasks/1/volunteers"))
                    .andExpect(status().isNotFound());

            verify(participationService).getTaskVolunteers(1L);
        }

        @Test
        @DisplayName("Should require authentication")
        void shouldRequireAuthentication() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/tasks/1/volunteers"))
                    .andExpect(status().isUnauthorized());

            verify(participationService, never()).getTaskVolunteers(anyLong());
        }
    }

    @Nested
    @DisplayName("Check Participation Status Tests")
    class CheckParticipationStatusTests {

        @Test
        @DisplayName("Should return true when user is participating")
        @WithMockUser
        void shouldReturnTrueWhenUserIsParticipating() throws Exception {
            // Given
            when(participationService.isUserParticipating(eq(1L), anyString())).thenReturn(true);

            // When & Then
            mockMvc.perform(get("/api/tasks/1/participation-status"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));

            verify(participationService).isUserParticipating(eq(1L), anyString());
        }

        @Test
        @DisplayName("Should return false when user is not participating")
        @WithMockUser
        void shouldReturnFalseWhenUserIsNotParticipating() throws Exception {
            // Given
            when(participationService.isUserParticipating(eq(1L), anyString())).thenReturn(false);

            // When & Then
            mockMvc.perform(get("/api/tasks/1/participation-status"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("false"));

            verify(participationService).isUserParticipating(eq(1L), anyString());
        }

        @Test
        @DisplayName("Should return 404 when task not found")
        @WithMockUser
        void shouldReturn404WhenTaskNotFound() throws Exception {
            // Given
            when(participationService.isUserParticipating(eq(1L), anyString()))
                    .thenThrow(new BusinessException("Task not found", "TASK_NOT_FOUND"));

            // When & Then
            mockMvc.perform(get("/api/tasks/1/participation-status"))
                    .andExpect(status().isNotFound());

            verify(participationService).isUserParticipating(eq(1L), anyString());
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        @WithMockUser
        void shouldReturn404WhenUserNotFound() throws Exception {
            // Given
            when(participationService.isUserParticipating(eq(1L), anyString()))
                    .thenThrow(new BusinessException("User not found", "USER_NOT_FOUND"));

            // When & Then
            mockMvc.perform(get("/api/tasks/1/participation-status"))
                    .andExpect(status().isNotFound());

            verify(participationService).isUserParticipating(eq(1L), anyString());
        }

        @Test
        @DisplayName("Should require authentication")
        void shouldRequireAuthentication() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/tasks/1/participation-status"))
                    .andExpect(status().isUnauthorized());

            verify(participationService, never()).isUserParticipating(anyLong(), anyString());
        }
    }

    @Nested
    @DisplayName("Path Variable Validation Tests")
    class PathVariableValidationTests {

        @Test
        @DisplayName("Should handle invalid task ID")
        @WithMockUser(roles = "VOLUNTEER")
        void shouldHandleInvalidTaskId() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/tasks/invalid/participate"))
                    .andExpect(status().isBadRequest());

            verify(participationService, never()).joinTask(anyLong(), anyString());
        }

        @Test
        @DisplayName("Should handle negative task ID")
        @WithMockUser(roles = "VOLUNTEER")
        void shouldHandleNegativeTaskId() throws Exception {
            // Given
            doThrow(new BusinessException("Task not found", "TASK_NOT_FOUND"))
                    .when(participationService).joinTask(eq(-1L), anyString());

            // When & Then
            mockMvc.perform(post("/api/tasks/-1/participate"))
                    .andExpect(status().isNotFound());

            verify(participationService).joinTask(eq(-1L), anyString());
        }
    }
}
