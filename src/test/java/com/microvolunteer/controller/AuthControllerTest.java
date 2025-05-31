package com.microvolunteer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microvolunteer.config.KeycloakUtils;
import com.microvolunteer.dto.response.UserResponse;
import com.microvolunteer.enums.UserType;
import com.microvolunteer.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("Тести для AuthController (Keycloak версія)")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private KeycloakUtils keycloakUtils;

    private UserResponse userResponse;
    private final String testKeycloakId = "test-keycloak-id";
    private final String testUsername = "testuser";
    private final String testEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        userResponse = UserResponse.builder()
                .id(1L)
                .keycloakId(testKeycloakId)
                .username(testUsername)
                .email(testEmail)
                .userType(UserType.VOLUNTEER)
                .isActive(true)
                .dateJoined(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(username = "test-keycloak-id", roles = {"USER"})
    @DisplayName("Синхронізація користувача - успішно")
    void syncUser_Success() throws Exception {
        // Given
        when(keycloakUtils.getCurrentUsername()).thenReturn(Optional.of(testUsername));
        when(keycloakUtils.getCurrentUserEmail()).thenReturn(Optional.of(testEmail));
        when(authService.syncKeycloakUser(testKeycloakId, testUsername, testEmail))
                .thenReturn(userResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/sync")
                        .with(user(testKeycloakId).roles("USER")))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.keycloakId").value(testKeycloakId))
                .andExpect(jsonPath("$.username").value(testUsername))
                .andExpect(jsonPath("$.email").value(testEmail));
    }

    @Test
    @DisplayName("Синхронізація користувача - без автентифікації")
    void syncUser_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/sync"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test-keycloak-id", roles = {"USER"})
    @DisplayName("Повна синхронізація з JWT токена - успішно")
    void syncUserFull_Success() throws Exception {
        // Given
        String jwtToken = "Bearer valid.jwt.token";
        when(authService.syncFromJwtToken("valid.jwt.token")).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/sync-full")
                        .header("Authorization", jwtToken)
                        .with(user(testKeycloakId).roles("USER")))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.id").value(1))
                .andExpected(jsonPath("$.username").value(testUsername));
    }

    @Test
    @WithMockUser(username = "test-keycloak-id", roles = {"USER"})
    @DisplayName("Повна синхронізація - неправильний заголовок")
    void syncUserFull_InvalidAuthHeader() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/sync-full")
                        .header("Authorization", "Invalid token")
                        .with(user(testKeycloakId).roles("USER")))
                .andExpected(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test-keycloak-id", roles = {"USER"})
    @DisplayName("Генерація внутрішнього токена - успішно")
    void generateInternalToken_Success() throws Exception {
        // Given
        String internalToken = "internal.jwt.token";
        when(authService.generateInternalToken(testKeycloakId)).thenReturn(internalToken);

        // When & Then
        mockMvc.perform(post("/api/auth/token")
                        .with(user(testKeycloakId).roles("USER")))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.access_token").value(internalToken))
                .andExpected(jsonPath("$.token_type").value("Bearer"))
                .andExpected(jsonPath("$.expires_in").value("86400"))
                .andExpected(jsonPath("$.note").value("Internal token for service integration"));
    }

    @Test
    @WithMockUser(username = "test-keycloak-id", roles = {"USER"})
    @DisplayName("Отримання інформації про поточного користувача - успішно")
    void getCurrentUser_Success() throws Exception {
        // Given
        when(authService.getUserByKeycloakId(testKeycloakId)).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(get("/api/auth/me")
                        .with(user(testKeycloakId).roles("USER")))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.id").value(1))
                .andExpected(jsonPath("$.keycloakId").value(testKeycloakId))
                .andExpected(jsonPath("$.username").value(testUsername));
    }

    @Test
    @WithMockUser(username = "test-keycloak-id", roles = {"USER"})
    @DisplayName("Отримання інформації з Keycloak токена - успішно")
    void getKeycloakUserInfo_Success() throws Exception {
        // Given
        when(keycloakUtils.getCurrentUserKeycloakId()).thenReturn(Optional.of(testKeycloakId));
        when(keycloakUtils.getCurrentUsername()).thenReturn(Optional.of(testUsername));
        when(keycloakUtils.getCurrentUserEmail()).thenReturn(Optional.of(testEmail));

        // When & Then
        mockMvc.perform(get("/api/auth/user-info")
                        .with(user(testKeycloakId).roles("USER")))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.keycloak_id").value(testKeycloakId))
                .andExpected(jsonPath("$.username").value(testUsername))
                .andExpected(jsonPath("$.email").value(testEmail));
    }

    @Test
    @DisplayName("Всі захищені endpoints - без автентифікації")
    void protectedEndpoints_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/sync")).andExpected(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/sync-full")).andExpected(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/token")).andExpected(status().isUnauthorized());
        mockMvc.perform(get("/api/auth/me")).andExpected(status().isUnauthorized());
        mockMvc.perform(get("/api/auth/user-info")).andExpected(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test-keycloak-id", roles = {"WRONG_ROLE"})
    @DisplayName("Endpoints з неправильною роллю - заборонено")
    void protectedEndpoints_WrongRole() throws Exception {
        // Note: У нашій поточній конфігурації @PreAuthorize("hasRole('USER')"), 
        // але Spring Security автоматично додає префікс ROLE_, тому ROLE_USER = USER
        // Цей тест перевіряє випадок з неправильною роллю
        
        mockMvc.perform(post("/api/auth/sync")
                        .with(user(testKeycloakId).roles("WRONG_ROLE")))
                .andExpected(status().isForbidden());
    }
}