package com.microvolunteer.service;

import com.microvolunteer.dto.response.UserResponse;
import com.microvolunteer.entity.User;
import com.microvolunteer.enums.UserType;
import com.microvolunteer.mapper.UserMapper;
import com.microvolunteer.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void getAllUsers_ShouldReturnListOfUserResponses() {
        // Given
        User user = createTestUser();
        UserResponse userResponse = createTestUserResponse();
        
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // When
        List<UserResponse> result = userService.getAllUsers();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userResponse, result.get(0));
        verify(userRepository).findAll();
        verify(userMapper).toResponse(user);
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUserResponse() {
        // Given
        Long userId = 1L;
        User user = createTestUser();
        UserResponse userResponse = createTestUserResponse();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // When
        UserResponse result = userService.getUserById(userId);

        // Then
        assertNotNull(result);
        assertEquals(userResponse, result);
        verify(userRepository).findById(userId);
        verify(userMapper).toResponse(user);
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldThrowException() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(Exception.class, () -> userService.getUserById(userId));
        verify(userRepository).findById(userId);
        verifyNoInteractions(userMapper);
    }

    private User createTestUser() {
        return User.builder()
                .id(1L)
                .keycloakId("test-keycloak-id")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .userType(UserType.VOLUNTEER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private UserResponse createTestUserResponse() {
        return UserResponse.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .userType(UserType.VOLUNTEER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
