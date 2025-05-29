package com.microvolunteer.controller;

import com.microvolunteer.dto.response.UserResponse;
import com.microvolunteer.enums.UserType;
import com.microvolunteer.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userResponse = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .userType(UserType.VOLUNTEER)
                .isActive(true)
                .build();
    }

    @Test
    @WithMockUser
    void getCurrentUserProfile_Success() throws Exception {
        // Given
        when(userService.getUserByKeycloakId(anyString())).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void getUserById_Success() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void getUserStatistics_Success() throws Exception {
        // Given
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("userId", 1L);
        statistics.put("totalParticipations", 10L);

        when(userService.getUserStatistics(anyLong())).thenReturn(statistics);

        // When & Then
        mockMvc.perform(get("/api/users/1/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.totalParticipations").value(10));
    }
}
