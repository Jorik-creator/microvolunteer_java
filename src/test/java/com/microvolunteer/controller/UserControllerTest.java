package com.microvolunteer.controller;

import com.microvolunteer.dto.response.UserResponse;
import com.microvolunteer.enums.UserType;
import com.microvolunteer.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("Тести для UserController (Keycloak версія)")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private UserResponse userResponse;
    private final String testKeycloakId = "test-keycloak-id";

    @BeforeEach
    void setUp() {
        userResponse = UserResponse.builder()
                .id(1L)
                .keycloakId(testKeycloakId)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .userType(UserType.VOLUNTEER)
                .isActive(true)
                .dateJoined(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(username = "test-keycloak-id", roles = {"USER"})
    @DisplayName("Отримання профілю поточного користувача - успішно")
    void getCurrentUserProfile_Success() throws Exception {
        // Given
        when(userService.getUserByKeycloakId(testKeycloakId)).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(get("/api/users/profile")
                        .with(user(testKeycloakId).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.keycloakId").value(testKeycloakId))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"));
    }

    @Test
    @DisplayName("Отримання профілю - без автентифікації")
    void getCurrentUserProfile_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Отримання користувача за ID - успішно")
    void getUserById_Success() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("Отримання статистики користувача - успішно")
    void getUserStatistics_Success() throws Exception {
        // Given
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("userId", 1L);
        statistics.put("totalParticipations", 10L);
        statistics.put("completedTasks", 8L);
        statistics.put("activeTasks", 2L);
        statistics.put("createdTasks", 5L);

        when(userService.getUserStatistics(anyLong())).thenReturn(statistics);

        // When & Then
        mockMvc.perform(get("/api/users/1/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.totalParticipations").value(10))
                .andExpect(jsonPath("$.completedTasks").value(8))
                .andExpect(jsonPath("$.activeTasks").value(2))
                .andExpect(jsonPath("$.createdTasks").value(5));
    }

    @Test
    @DisplayName("Отримання статистики - користувач не знайдений")
    void getUserStatistics_UserNotFound() throws Exception {
        // Given
        when(userService.getUserStatistics(999L)).thenThrow(new RuntimeException("User not found"));

        // When & Then
        mockMvc.perform(get("/api/users/999/statistics"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "test-keycloak-id", roles = {"ADMIN"})
    @DisplayName("Отримання профілю з правами адміністратора")
    void getCurrentUserProfile_AdminRole() throws Exception {
        // Given
        UserResponse adminUser = UserResponse.builder()
                .id(2L)
                .keycloakId(testKeycloakId)
                .username("adminuser")
                .email("admin@example.com")
                .userType(UserType.ORGANIZATION)
                .isActive(true)
                .build();

        when(userService.getUserByKeycloakId(testKeycloakId)).thenReturn(adminUser);

        // When & Then
        mockMvc.perform(get("/api/users/profile")
                        .with(user(testKeycloakId).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("adminuser"))
                .andExpect(jsonPath("$.userType").value("ORGANIZATION"));
    }

    @Test
    @WithMockUser(username = "test-keycloak-id", roles = {"WRONG_ROLE"})
    @DisplayName("Захищені endpoints з неправильною роллю")
    void protectedEndpoints_WrongRole() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/profile")
                        .with(user(testKeycloakId).roles("WRONG_ROLE")))
                .andExpect(status().isForbidden());
    }
}