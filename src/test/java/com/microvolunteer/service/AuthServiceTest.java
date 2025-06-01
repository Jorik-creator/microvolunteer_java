package com.microvolunteer.service;

import com.microvolunteer.config.KeycloakUtils;
import com.microvolunteer.dto.request.UserRegistrationRequest;
import com.microvolunteer.dto.response.UserResponse;
import com.microvolunteer.entity.User;
import com.microvolunteer.enums.UserType;
import com.microvolunteer.exception.BusinessException;
import com.microvolunteer.mapper.UserMapper;
import com.microvolunteer.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private KeycloakUtils keycloakUtils;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerUser_ValidRequest_ShouldReturnUserResponse() {
        // Given
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("test@example.com");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setPassword("password");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("test@example.com");
        savedUser.setFirstName("Test");
        savedUser.setLastName("User");
        savedUser.setUserType(UserType.VOLUNTEER);
        savedUser.setKeycloakId("keycloak-id-123");

        UserResponse expectedResponse = new UserResponse();
        expectedResponse.setId(1L);
        expectedResponse.setEmail("test@example.com");
        expectedResponse.setFirstName("Test");
        expectedResponse.setLastName("User");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(keycloakUtils.createUser(anyString(), anyString(), anyString(), anyString())).thenReturn("keycloak-id-123");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(expectedResponse);

        // When
        UserResponse result = authService.registerUser(request);

        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test", result.getFirstName());
        assertEquals("User", result.getLastName());
        
        verify(userRepository).findByEmail("test@example.com");
        verify(keycloakUtils).createUser("test@example.com", "password", "Test", "User");
        verify(userRepository).save(any(User.class));
        verify(userMapper).toResponse(savedUser);
    }

    @Test
    void registerUser_EmailAlreadyExists_ShouldThrowException() {
        // Given
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("existing@example.com");
        request.setFirstName("Test");
        request.setLastName("User");

        User existingUser = new User();
        existingUser.setEmail("existing@example.com");

        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));

        // When & Then
        BusinessException exception = assertThrows(
            BusinessException.class, 
            () -> authService.registerUser(request)
        );

        assertEquals("User with email existing@example.com already exists", exception.getMessage());
        verify(userRepository).findByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
        verify(keycloakUtils, never()).createUser(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void findByEmail_ValidEmail_ShouldReturnUser() {
        // Given
        String email = "test@example.com";
        User user = new User();
        user.setId(1L);
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When
        Optional<User> result = authService.findByEmail(email);

        // Then
        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void findByEmail_InvalidEmail_ShouldReturnEmpty() {
        // Given
        String email = "invalid@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        Optional<User> result = authService.findByEmail(email);

        // Then
        assertTrue(result.isEmpty());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void findByKeycloakId_ValidKeycloakId_ShouldReturnUser() {
        // Given
        String keycloakId = "keycloak-id-123";
        User user = new User();
        user.setId(1L);
        user.setKeycloakId(keycloakId);
        user.setEmail("test@example.com");

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(user));

        // When
        Optional<User> result = authService.findByKeycloakId(keycloakId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(keycloakId, result.get().getKeycloakId());
        verify(userRepository).findByKeycloakId(keycloakId);
    }

    @Test
    void findByKeycloakId_InvalidKeycloakId_ShouldReturnEmpty() {
        // Given
        String keycloakId = "invalid-keycloak-id";
        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());

        // When
        Optional<User> result = authService.findByKeycloakId(keycloakId);

        // Then
        assertTrue(result.isEmpty());
        verify(userRepository).findByKeycloakId(keycloakId);
    }

    @Test
    void updateLastLogin_ExistingUser_ShouldUpdateLoginTime() {
        // Given
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setCreatedAt(LocalDateTime.now().minusDays(1));
        user.setUpdatedAt(LocalDateTime.now().minusDays(1));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        authService.updateLastLogin(userId);

        // Then
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deactivateUser_ExistingUser_ShouldDeactivateUser() {
        // Given
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setIsActive(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        authService.deactivateUser(userId);

        // Then
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }
}
