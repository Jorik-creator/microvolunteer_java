package com.microvolunteer.controller;

import com.microvolunteer.dto.response.UserResponse;
import com.microvolunteer.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser(roles = "USER")
    void getCurrentUserProfile_ShouldReturnUserProfile() throws Exception {
        // Given
        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setEmail("test@example.com");
        response.setFirstName("Test");
        response.setLastName("User");

        when(userService.getUserByKeycloakId(anyString())).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Test"));
    }

    @Test
    void getUserById_ExistingUser_ShouldReturnUser() throws Exception {
        // Given
        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setEmail("test@example.com");

        when(userService.getUserById(1L)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getUserStatistics_ShouldReturnStatistics() throws Exception {
        // Given
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("tasksCompleted", 5);
        statistics.put("tasksCreated", 2);

        when(userService.getUserStatistics(anyLong())).thenReturn(statistics);

        // When & Then
        mockMvc.perform(get("/api/users/1/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasksCompleted").value(5))
                .andExpect(jsonPath("$.tasksCreated").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_WithAdminRole_ShouldReturnAllUsers() throws Exception {
        // Given
        UserResponse user1 = new UserResponse();
        user1.setId(1L);
        user1.setEmail("user1@example.com");
        
        UserResponse user2 = new UserResponse();
        user2.setId(2L);
        user2.setEmail("user2@example.com");

        List<UserResponse> users = Arrays.asList(user1, user2);
        when(userService.getAllUsers()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].email").value("user1@example.com"));
    }

    @Test
    void getActiveUsers_ShouldReturnActiveUsers() throws Exception {
        // Given
        UserResponse activeUser = new UserResponse();
        activeUser.setId(1L);
        activeUser.setEmail("active@example.com");

        List<UserResponse> activeUsers = Arrays.asList(activeUser);
        when(userService.getActiveUsers()).thenReturn(activeUsers);

        // When & Then
        mockMvc.perform(get("/api/users/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("active@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivateUser_WithAdminRole_ShouldReturnOk() throws Exception {
        // Given
        when(userService.deactivateUser(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(put("/api/users/1/deactivate"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivateUser_UserNotFound_ShouldReturnNotFound() throws Exception {
        // Given
        when(userService.deactivateUser(999L)).thenReturn(false);

        // When & Then
        mockMvc.perform(put("/api/users/999/deactivate"))
                .andExpect(status().isNotFound());
    }
}
