package com.microvolunteer.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microvolunteer.dto.response.UserResponse;
import com.microvolunteer.entity.User;
import com.microvolunteer.repository.UserRepository;
import com.microvolunteer.service.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.jwt.secret=test-secret-key-that-should-be-at-least-256-bits-long-for-hmac-sha256",
        "app.jwt.expiration=86400000"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Інтеграційні тести для Keycloak аутентифікації")
class KeycloakAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private final String testSecretKey = "test-secret-key-that-should-be-at-least-256-bits-long-for-hmac-sha256";
    private final String testKeycloakId = "integration-test-keycloak-id";
    private final String testUsername = "integrationuser";
    private final String testEmail = "integration@example.com";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    @DisplayName("Повний потік: синхронізація користувача з JWT токена")
    void fullFlow_SyncUserFromJwtToken() throws Exception {
        // Given
        String keycloakToken = createKeycloakToken(testKeycloakId, testUsername, testEmail, 
                "John", "Doe", List.of("USER", "VOLUNTEER"));

        // When & Then
        mockMvc.perform(post("/api/auth/sync-full")
                        .header("Authorization", "Bearer " + keycloakToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keycloakId").value(testKeycloakId))
                .andExpect(jsonPath("$.username").value(testUsername))
                .andExpect(jsonPath("$.email").value(testEmail))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));

        // Перевірка в базі даних
        User savedUser = userRepository.findByKeycloakId(testKeycloakId).orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo(testUsername);
        assertThat(savedUser.getEmail()).isEqualTo(testEmail);
        assertThat(savedUser.getFirstName()).isEqualTo("John");
        assertThat(savedUser.getLastName()).isEqualTo("Doe");
    }

    @Test
    @Transactional
    @DisplayName("Повний потік: отримання інформації про користувача")
    void fullFlow_GetUserInfo() throws Exception {
        // Given
        // Спочатку створюємо користувача в БД
        User user = User.builder()
                .keycloakId(testKeycloakId)
                .username(testUsername)
                .email(testEmail)
                .firstName("Test")
                .lastName("User")
                .password("")
                .isActive(true)
                .dateJoined(LocalDateTime.now())
                .build();
        userRepository.save(user);

        String keycloakToken = createKeycloakToken(testKeycloakId, testUsername, testEmail, 
                "Test", "User", List.of("USER"));

        // When & Then
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + keycloakToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keycloakId").value(testKeycloakId))
                .andExpect(jsonPath("$.username").value(testUsername))
                .andExpect(jsonPath("$.email").value(testEmail));
    }

    @Test
    @Transactional
    @DisplayName("Повний потік: створення завдання з аутентифікацією")
    void fullFlow_CreateTaskWithAuthentication() throws Exception {
        // Given
        // Створюємо користувача
        User user = User.builder()
                .keycloakId(testKeycloakId)
                .username(testUsername)
                .email(testEmail)
                .password("")
                .isActive(true)
                .dateJoined(LocalDateTime.now())
                .build();
        userRepository.save(user);

        String keycloakToken = createKeycloakToken(testKeycloakId, testUsername, testEmail, 
                "Test", "User", List.of("USER"));

        String taskJson = """
                {
                    "title": "Integration Test Task",
                    "description": "Test task description",
                    "categoryId": 1,
                    "maxVolunteers": 5,
                    "requiredSkills": "Testing"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + keycloakToken)
                        .contentType("application/json")
                        .content(taskJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Integration Test Task"))
                .andExpect(jsonPath("$.description").value("Test task description"));
    }

    @Test
    @DisplayName("Неавторизований доступ до захищених endpoint-ів")
    void unauthorizedAccess_ToProtectedEndpoints() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/sync"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/tasks")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Невалідний JWT токен")
    void invalidJwtToken_ReturnsUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    @DisplayName("Оновлення існуючого користувача при синхронізації")
    void updateExistingUser_OnSync() throws Exception {
        // Given
        // Створюємо користувача з старими даними
        User existingUser = User.builder()
                .keycloakId(testKeycloakId)
                .username("oldusername")
                .email("old@example.com")
                .firstName("Old")
                .lastName("Name")
                .password("")
                .isActive(true)
                .dateJoined(LocalDateTime.now().minusDays(1))
                .build();
        userRepository.save(existingUser);

        // Новий токен з оновленими даними
        String keycloakToken = createKeycloakToken(testKeycloakId, "newusername", "new@example.com", 
                "New", "Name", List.of("USER"));

        // When
        mockMvc.perform(post("/api/auth/sync-full")
                        .header("Authorization", "Bearer " + keycloakToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newusername"))
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.firstName").value("New"))
                .andExpect(jsonPath("$.lastName").value("Name"));

        // Then
        User updatedUser = userRepository.findByKeycloakId(testKeycloakId).orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getUsername()).isEqualTo("newusername");
        assertThat(updatedUser.getEmail()).isEqualTo("new@example.com");
        assertThat(updatedUser.getFirstName()).isEqualTo("New");
        assertThat(updatedUser.getLastName()).isEqualTo("Name");
        assertThat(updatedUser.getLastLogin()).isNotNull();
    }

    @Test
    @DisplayName("Доступ до публічних endpoint-ів без аутентифікації")
    void publicEndpoints_AccessibleWithoutAuth() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/tasks/list"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/tasks/recent"))
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    @DisplayName("Генерація внутрішнього токена")
    void generateInternalToken_Success() throws Exception {
        // Given
        User user = User.builder()
                .keycloakId(testKeycloakId)
                .username(testUsername)
                .email(testEmail)
                .password("")
                .isActive(true)
                .dateJoined(LocalDateTime.now())
                .build();
        userRepository.save(user);

        String keycloakToken = createKeycloakToken(testKeycloakId, testUsername, testEmail, 
                "Test", "User", List.of("USER"));

        // When & Then
        mockMvc.perform(post("/api/auth/token")
                        .header("Authorization", "Bearer " + keycloakToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.expires_in").value("86400"));
    }

    // Helper method для створення Keycloak токена
    private String createKeycloakToken(String keycloakId, String username, String email, 
                                     String firstName, String lastName, List<String> roles) {
        SecretKey key = Keys.hmacShaKeyFor(testSecretKey.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(keycloakId)
                .claim("preferred_username", username)
                .claim("email", email)
                .claim("given_name", firstName)
                .claim("family_name", lastName)
                .claim("name", firstName + " " + lastName)
                .claim("realm_access", Map.of("roles", roles))
                .claim("resource_access", Map.of("microvolunteer", Map.of("roles", roles)))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(key)
                .compact();
    }
}