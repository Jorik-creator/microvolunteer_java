package org.example.service;

import org.example.dto.UserResponse;
import org.example.dto.UserStatisticsResponse;
import org.example.dto.UserUpdateRequest;
import org.example.exception.BadRequestException;
import org.example.exception.ResourceNotFoundException;
import org.example.model.TaskStatus;
import org.example.model.User;
import org.example.model.UserType;
import org.example.repository.UserRepository;
import org.example.util.EntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private EntityMapper entityMapper;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;
    
    private User user;
    private UserResponse userResponse;
    private UserUpdateRequest userUpdateRequest;
    
    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .userType(UserType.VOLUNTEER)
                .phone("+1234567890")
                .bio("Test bio")
                .address("Test address")
                .profileImageUrl("http://example.com/image.jpg")
                .dateJoined(LocalDateTime.now().minusDays(30))
                .lastUpdated(LocalDateTime.now())
                .isActive(true)
                .createdTasks(new HashSet<>())
                .build();
        
        userResponse = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .userType("VOLUNTEER")
                .phone("+1234567890")
                .bio("Test bio")
                .address("Test address")
                .profileImageUrl("http://example.com/image.jpg")
                .dateJoined(LocalDateTime.now().minusDays(30))
                .lastUpdated(LocalDateTime.now())
                .isActive(true)
                .build();
        
        userUpdateRequest = UserUpdateRequest.builder()
                .email("updated@example.com")
                .firstName("Updated")
                .lastName("User")
                .phone("+9876543210")
                .bio("Updated bio")
                .address("Updated address")
                .profileImageUrl("http://example.com/updated-image.jpg")
                .build();
    }
    
    @Test
    void getAllUsers_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(user));
        
        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(entityMapper.toUserResponse(user)).thenReturn(userResponse);
        
        Page<UserResponse> result = userService.getAllUsers(null, null, null, null, pageable);
        
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(userResponse, result.getContent().get(0));
        verify(userRepository).findAll(pageable);
        verify(entityMapper).toUserResponse(user);
    }
    
    @Test
    void getUserByUsername_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(entityMapper.toUserResponse(user)).thenReturn(userResponse);
        
        UserResponse result = userService.getUserByUsername("testuser");
        
        assertNotNull(result);
        assertEquals(userResponse.getUsername(), result.getUsername());
        assertEquals(userResponse.getEmail(), result.getEmail());
        verify(userRepository).findByUsername("testuser");
        verify(entityMapper).toUserResponse(user);
    }
    
    @Test
    void getUserByUsername_NotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserByUsername("nonexistent")
        );
        
        assertEquals("User not found with username: nonexistent", exception.getMessage());
        verify(userRepository).findByUsername("nonexistent");
        verify(entityMapper, never()).toUserResponse(any());
    }
    
    @Test
    void updateProfile_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);
        when(entityMapper.toUserResponse(user)).thenReturn(userResponse);
        
        UserResponse result = userService.updateProfile("testuser", userUpdateRequest);
        
        assertNotNull(result);
        verify(userRepository).findByUsername("testuser");
        verify(userRepository).existsByEmail("updated@example.com");
        verify(entityMapper).updateUserFromRequest(user, userUpdateRequest);
        verify(userRepository).save(user);
        verify(entityMapper).toUserResponse(user);
    }
    
    @Test
    void updateProfile_UserNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.updateProfile("nonexistent", userUpdateRequest)
        );
        
        assertEquals("User not found with username: nonexistent", exception.getMessage());
        verify(userRepository).findByUsername("nonexistent");
        verify(userRepository, never()).save(any());
    }
    
    @Test
    void updateProfile_EmailAlreadyExists_ThrowsBadRequestException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(true);
        
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> userService.updateProfile("testuser", userUpdateRequest)
        );
        
        assertEquals("Email is already in use!", exception.getMessage());
        verify(userRepository).findByUsername("testuser");
        verify(userRepository).existsByEmail("updated@example.com");
        verify(userRepository, never()).save(any());
    }
    
    @Test
    void getUserStatistics_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user)); // Add this mock
        when(userRepository.countTasksByUserIdAndStatus(1L, TaskStatus.COMPLETED)).thenReturn(3L);
        when(userRepository.countTasksByUserIdAndStatus(1L, TaskStatus.CANCELLED)).thenReturn(1L);
        when(userRepository.countCompletedParticipationsByUserId(1L, TaskStatus.COMPLETED)).thenReturn(5L);
        when(userRepository.countVolunteersHelpedByUserId(1L)).thenReturn(2L);
        
        UserStatisticsResponse result = userService.getUserStatistics("testuser");
        
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("testuser", result.getUsername());
        assertEquals("VOLUNTEER", result.getUserType());
        assertEquals(0L, result.getTotalCreatedTasks());
        assertEquals(3L, result.getTotalCompletedTasks());
        assertEquals(1L, result.getTotalCancelledTasks());
        assertEquals(5L, result.getTotalParticipatedTasks());
        assertEquals(2L, result.getTotalVolunteersHelped());
        verify(userRepository).findByUsername("testuser");
        verify(userRepository).findById(1L);
        verify(userRepository).countTasksByUserIdAndStatus(1L, TaskStatus.COMPLETED);
        verify(userRepository).countTasksByUserIdAndStatus(1L, TaskStatus.CANCELLED);
        verify(userRepository).countCompletedParticipationsByUserId(1L, TaskStatus.COMPLETED);
        verify(userRepository).countVolunteersHelpedByUserId(1L);
    }
    
    @Test
    void getUserStatistics_UserNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserStatistics("nonexistent")
        );
        
        assertEquals("User not found with username: nonexistent", exception.getMessage());
        verify(userRepository).findByUsername("nonexistent");
        verify(userRepository, never()).countTasksByUserIdAndStatus(any(), any());
        verify(userRepository, never()).countCompletedParticipationsByUserId(any(), any());
        verify(userRepository, never()).countVolunteersHelpedByUserId(any());
    }
    
    @Test
    void updateProfile_EmailNotChanged_Success() {
        userUpdateRequest.setEmail("test@example.com"); // Same email as user
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(entityMapper.toUserResponse(user)).thenReturn(userResponse);
        
        UserResponse result = userService.updateProfile("testuser", userUpdateRequest);
        
        assertNotNull(result);
        verify(userRepository).findByUsername("testuser");
        verify(userRepository, never()).existsByEmail(any());
        verify(entityMapper).updateUserFromRequest(user, userUpdateRequest);
        verify(userRepository).save(user);
        verify(entityMapper).toUserResponse(user);
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(entityMapper.toUserResponse(user)).thenReturn(userResponse);
        
        UserResponse result = userService.getUserById(1L);
        
        assertNotNull(result);
        assertEquals(userResponse.getId(), result.getId());
        verify(userRepository).findById(1L);
        verify(entityMapper).toUserResponse(user);
    }
    
    @Test
    void getUserById_NotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserById(1L)
        );
        
        assertEquals("User not found with id: 1", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(entityMapper, never()).toUserResponse(any());
    }
    
    @Test
    void existsByUsername_True() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        
        boolean result = userService.existsByUsername("testuser");
        
        assertTrue(result);
        verify(userRepository).existsByUsername("testuser");
    }
    
    @Test
    void existsByEmail_False() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        
        boolean result = userService.existsByEmail("test@example.com");
        
        assertFalse(result);
        verify(userRepository).existsByEmail("test@example.com");
    }
}