package com.microvolunteer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microvolunteer.config.TestSecurityConfig;
import com.microvolunteer.dto.request.UserRegistrationRequest;
import com.microvolunteer.dto.response.UserResponse;
import com.microvolunteer.enums.UserType;
import com.microvolunteer.exception.BusinessException;
import com.microvolunteer.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("UserController Integration Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserRegistrationRequest registrationRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        registrationRequest = new UserRegistrationRequest();
        registrationRequest.setFirstName("John");
        registrationRequest.setLastName("Doe");
        registrationRequest.setEmail("test@example.com");
        registrationRequest.setUserType(UserType.VOLUNTEER);
        registrationRequest.setPhone("+1234567890");

        userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setFirstName("John");
        userResponse.setLastName("Doe");
        userResponse.setEmail("test@example.com");
        userResponse.setUserType(UserType.VOLUNTEER);
        userResponse.setPhone("+1234567890");
        userResponse.setActive(true);
    }

    @Nested
    @DisplayName("User Registration Tests")
    class UserRegistrationTests {

        @Test
        @DisplayName("Should register user successfully")
        @WithMockUser
        void shouldRegisterUserSuccessfully() throws Exception {
            // Given
            when(userService.registerUser(any(UserRegistrationRequest.class), anyString()))
                    .thenReturn(userResponse);

            // When & Then
            mockMvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registrationRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Doe"))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.userType").value("VOLUNTEER"))
                    .andExpect(jsonPath("$.active").value(true));

            verify(userService).registerUser(any(UserRegistrationRequest.class), anyString());
        }

        @Test
        @DisplayName("Should return 400 when registration request is invalid")
        @WithMockUser
        void shouldReturn400WhenRegistrationRequestInvalid() throws Exception {
            // Given
            UserRegistrationRequest invalidRequest = new UserRegistrationRequest();
            // Missing required fields

            // When & Then
            mockMvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).registerUser(any(UserRegistrationRequest.class), anyString());
        }

        @Test
        @DisplayName("Should return 409 when user already exists")
        @WithMockUser
        void shouldReturn409WhenUserAlreadyExists() throws Exception {
            // Given
            when(userService.registerUser(any(UserRegistrationRequest.class), anyString()))
                    .thenThrow(new BusinessException("User already exists", "USER_ALREADY_EXISTS"));

            // When & Then
            mockMvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registrationRequest)))
                    .andExpect(status().isConflict());

            verify(userService).registerUser(any(UserRegistrationRequest.class), anyString());
        }
    }

    @Nested
    @DisplayName("Get User Profile Tests")
    class GetUserProfileTests {

        @Test
        @DisplayName("Should get user profile successfully")
        @WithMockUser
        void shouldGetUserProfileSuccessfully() throws Exception {
            // Given
            when(userService.getUserByKeycloakSubject(anyString())).thenReturn(userResponse);

            // When & Then
            mockMvc.perform(get("/api/users/profile"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Doe"))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.userType").value("VOLUNTEER"));

            verify(userService).getUserByKeycloakSubject(anyString());
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        @WithMockUser
        void shouldReturn404WhenUserNotFound() throws Exception {
            // Given
            when(userService.getUserByKeycloakSubject(anyString()))
                    .thenThrow(new BusinessException("User not found", "USER_NOT_FOUND"));

            // When & Then
            mockMvc.perform(get("/api/users/profile"))
                    .andExpect(status().isNotFound());

            verify(userService).getUserByKeycloakSubject(anyString());
        }
    }

    @Nested
    @DisplayName("Update User Profile Tests")
    class UpdateUserProfileTests {

        @Test
        @DisplayName("Should update user profile successfully")
        @WithMockUser
        void shouldUpdateUserProfileSuccessfully() throws Exception {
            // Given
            UserRegistrationRequest updateRequest = new UserRegistrationRequest();
            updateRequest.setFirstName("Jane");
            updateRequest.setLastName("Smith");
            updateRequest.setEmail("jane@example.com");
            updateRequest.setUserType(UserType.VOLUNTEER);

            UserResponse updatedResponse = new UserResponse();
            updatedResponse.setId(1L);
            updatedResponse.setFirstName("Jane");
            updatedResponse.setLastName("Smith");
            updatedResponse.setEmail("jane@example.com");
            updatedResponse.setUserType(UserType.VOLUNTEER);

            when(userService.updateUser(anyString(), any(UserRegistrationRequest.class)))
                    .thenReturn(updatedResponse);

            // When & Then
            mockMvc.perform(put("/api/users/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("Jane"))
                    .andExpect(jsonPath("$.lastName").value("Smith"))
                    .andExpect(jsonPath("$.email").value("jane@example.com"));

            verify(userService).updateUser(anyString(), any(UserRegistrationRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when update request is invalid")
        @WithMockUser
        void shouldReturn400WhenUpdateRequestInvalid() throws Exception {
            // Given
            UserRegistrationRequest invalidRequest = new UserRegistrationRequest();
            // Missing required fields

            // When & Then
            mockMvc.perform(put("/api/users/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).updateUser(anyString(), any(UserRegistrationRequest.class));
        }

        @Test
        @DisplayName("Should return 409 when email already exists")
        @WithMockUser
        void shouldReturn409WhenEmailAlreadyExists() throws Exception {
            // Given
            when(userService.updateUser(anyString(), any(UserRegistrationRequest.class)))
                    .thenThrow(new BusinessException("User already exists", "USER_ALREADY_EXISTS"));

            // When & Then
            mockMvc.perform(put("/api/users/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registrationRequest)))
                    .andExpect(status().isConflict());

            verify(userService).updateUser(anyString(), any(UserRegistrationRequest.class));
        }
    }

    @Nested
    @DisplayName("Get User by ID Tests")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should get user by ID successfully")
        @WithMockUser
        void shouldGetUserByIdSuccessfully() throws Exception {
            // Given
            when(userService.getUserById(1L)).thenReturn(userResponse);

            // When & Then
            mockMvc.perform(get("/api/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Doe"));

            verify(userService).getUserById(1L);
        }

        @Test
        @DisplayName("Should return 404 when user not found by ID")
        @WithMockUser
        void shouldReturn404WhenUserNotFoundById() throws Exception {
            // Given
            when(userService.getUserById(1L))
                    .thenThrow(new BusinessException("User not found", "USER_NOT_FOUND"));

            // When & Then
            mockMvc.perform(get("/api/users/1"))
                    .andExpect(status().isNotFound());

            verify(userService).getUserById(1L);
        }
    }

    @Nested
    @DisplayName("Get Volunteers Tests")
    class GetVolunteersTests {

        @Test
        @DisplayName("Should get volunteers successfully")
        @WithMockUser
        void shouldGetVolunteersSuccessfully() throws Exception {
            // Given
            List<UserResponse> volunteers = List.of(userResponse);
            when(userService.getVolunteers()).thenReturn(volunteers);

            // When & Then
            mockMvc.perform(get("/api/users/volunteers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].userType").value("VOLUNTEER"));

            verify(userService).getVolunteers();
        }

        @Test
        @DisplayName("Should return empty list when no volunteers found")
        @WithMockUser
        void shouldReturnEmptyListWhenNoVolunteersFound() throws Exception {
            // Given
            when(userService.getVolunteers()).thenReturn(List.of());

            // When & Then
            mockMvc.perform(get("/api/users/volunteers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());

            verify(userService).getVolunteers();
        }
    }

    @Nested
    @DisplayName("Deactivate Account Tests")
    class DeactivateAccountTests {

        @Test
        @DisplayName("Should deactivate account successfully")
        @WithMockUser
        void shouldDeactivateAccountSuccessfully() throws Exception {
            // Given
            doNothing().when(userService).deactivateUser(anyString());

            // When & Then
            mockMvc.perform(delete("/api/users/profile"))
                    .andExpect(status().isNoContent());

            verify(userService).deactivateUser(anyString());
        }

        @Test
        @DisplayName("Should return 404 when user not found for deactivation")
        @WithMockUser
        void shouldReturn404WhenUserNotFoundForDeactivation() throws Exception {
            // Given
            doThrow(new BusinessException("User not found", "USER_NOT_FOUND"))
                    .when(userService).deactivateUser(anyString());

            // When & Then
            mockMvc.perform(delete("/api/users/profile"))
                    .andExpect(status().isNotFound());

            verify(userService).deactivateUser(anyString());
        }
    }

    @Nested
    @DisplayName("Authentication Required Tests")
    class AuthenticationRequiredTests {

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/users/profile"))
                    .andExpect(status().isUnauthorized());

            verify(userService, never()).getUserByKeycloakSubject(anyString());
        }

        @Test
        @DisplayName("Should return 401 for registration when not authenticated")
        void shouldReturn401ForRegistrationWhenNotAuthenticated() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registrationRequest)))
                    .andExpect(status().isUnauthorized());

            verify(userService, never()).registerUser(any(UserRegistrationRequest.class), anyString());
        }
    }
}
