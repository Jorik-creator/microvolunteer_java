package com.microvolunteer.integration;

import com.microvolunteer.dto.request.UserRegistrationRequest;
import com.microvolunteer.enums.UserType;
import com.microvolunteer.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class UserRegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void registerUser_PositiveScenario_ShouldCreateUserSuccessfully() throws Exception {
        // Given
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("newuser@test.com");
        request.setFirstName("New");
        request.setLastName("User");
        request.setPassword("SecurePassword123!");
        request.setUserType(UserType.VOLUNTEER);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newuser@test.com"))
                .andExpect(jsonPath("$.firstName").value("New"))
                .andExpect(jsonPath("$.lastName").value("User"));

        // Verify user was created in database
        assertTrue(userRepository.existsByEmail("newuser@test.com"));
    }

    @Test
    void registerUser_NegativeScenario_DuplicateEmail_ShouldReturnBadRequest() throws Exception {
        // Given - create first user
        UserRegistrationRequest firstRequest = new UserRegistrationRequest();
        firstRequest.setEmail("duplicate@test.com");
        firstRequest.setFirstName("First");
        firstRequest.setLastName("User");
        firstRequest.setPassword("Password123!");
        firstRequest.setUserType(UserType.VOLUNTEER);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // When - try to create second user with same email
        UserRegistrationRequest duplicateRequest = new UserRegistrationRequest();
        duplicateRequest.setEmail("duplicate@test.com");
        duplicateRequest.setFirstName("Second");
        duplicateRequest.setLastName("User");
        duplicateRequest.setPassword("AnotherPassword123!");
        duplicateRequest.setUserType(UserType.AFFECTED_PERSON);

        // Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_NegativeScenario_InvalidData_ShouldReturnBadRequest() throws Exception {
        // Given - request with missing required fields
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("invalid-email"); // Invalid email format
        // Missing first name, last name, password

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_NegativeScenario_EmptyRequest_ShouldReturnBadRequest() throws Exception {
        // Given - empty request
        UserRegistrationRequest request = new UserRegistrationRequest();

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
