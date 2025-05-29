package com.microvolunteer.service;

import com.microvolunteer.dto.response.UserResponse;
import com.microvolunteer.entity.User;
import com.microvolunteer.enums.UserType;
import com.microvolunteer.exception.BusinessException;
import com.microvolunteer.mapper.UserMapper;
import com.microvolunteer.repository.ParticipationRepository;
import com.microvolunteer.repository.TaskRepository;
import com.microvolunteer.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тести для UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ParticipationRepository participationRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserResponse testUserResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .keycloakId("test-keycloak-id")
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .userType(UserType.VOLUNTEER)
                .phone("+380501234567")
                .bio("Тестовий користувач")
                .address("Київ, Україна")
                .profileImage("avatar.jpg")
                .isActive(true)
                .dateJoined(LocalDateTime.now())
                .build();

        testUserResponse = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .userType(UserType.VOLUNTEER)
                .phone("+380501234567")
                .bio("Тестовий користувач")
                .address("Київ, Україна")
                .profileImage("avatar.jpg")
                .dateJoined(LocalDateTime.now())
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Отримання користувача за ID - успішно")
    void getUserById_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // When
        UserResponse result = userService.getUserById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUserResponse.getId());
        assertThat(result.getUsername()).isEqualTo(testUserResponse.getUsername());

        verify(userRepository).findById(1L);
        verify(userMapper).toResponse(testUser);
    }

    @Test
    @DisplayName("Отримання користувача за ID - не знайдено")
    void getUserById_NotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Користувача не знайдено");

        verify(userRepository).findById(1L);
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("Отримання користувача за Keycloak ID - успішно")
    void getUserByKeycloakId_Success() {
        // Given
        when(userRepository.findByKeycloakId(testUser.getKeycloakId())).thenReturn(Optional.of(testUser));
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // When
        UserResponse result = userService.getUserByKeycloakId(testUser.getKeycloakId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUserResponse.getId());
        assertThat(result.getUsername()).isEqualTo(testUserResponse.getUsername());

        verify(userRepository).findByKeycloakId(testUser.getKeycloakId());
        verify(userMapper).toResponse(testUser);
    }

    @Test
    @DisplayName("Отримання користувача за Keycloak ID - не знайдено")
    void getUserByKeycloakId_NotFound() {
        // Given
        when(userRepository.findByKeycloakId(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserByKeycloakId("invalid-keycloak-id"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Користувача не знайдено");

        verify(userRepository).findByKeycloakId("invalid-keycloak-id");
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("Отримання статистики користувача - успішно")
    void getUserStatistics_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(participationRepository.countByUserId(1L)).thenReturn(5L);

        // When
        Map<String, Object> result = userService.getUserStatistics(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get("userId")).isEqualTo(1L);
        assertThat(result.get("username")).isEqualTo("testuser");
        assertThat(result.get("userType")).isEqualTo(UserType.VOLUNTEER);
        assertThat(result.get("totalParticipations")).isEqualTo(5L);

        verify(userRepository).findById(1L);
        verify(participationRepository).countByUserId(1L);
    }

    @Test
    @DisplayName("Отримання статистики користувача - не знайдено")
    void getUserStatistics_NotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserStatistics(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Користувача не знайдено");

        verify(userRepository).findById(1L);
        verifyNoInteractions(participationRepository);
    }

    @Test
    @DisplayName("Оновлення профілю - успішно")
    void updateProfile_Success() {
        // Given
        Map<String, String> updates = new HashMap<>();
        updates.put("firstName", "Updated");
        updates.put("lastName", "Name");
        updates.put("bio", "Оновлена біографія");
        updates.put("address", "Львів, Україна");
        updates.put("phone", "+380501111111");

        when(userRepository.findByKeycloakId(testUser.getKeycloakId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // When
        UserResponse result = userService.updateProfile(testUser.getKeycloakId(), updates);

        // Then
        assertThat(result).isNotNull();
        assertThat(testUser.getFirstName()).isEqualTo("Updated");
        assertThat(testUser.getLastName()).isEqualTo("Name");
        assertThat(testUser.getBio()).isEqualTo("Оновлена біографія");
        assertThat(testUser.getAddress()).isEqualTo("Львів, Україна");
        assertThat(testUser.getPhone()).isEqualTo("+380501111111");

        verify(userRepository).findByKeycloakId(testUser.getKeycloakId());
        verify(userRepository).save(testUser);
        verify(userMapper).toResponse(testUser);
    }

    @Test
    @DisplayName("Оновлення профілю - не знайдено")
    void updateProfile_NotFound() {
        // Given
        Map<String, String> updates = new HashMap<>();
        updates.put("firstName", "Updated");

        when(userRepository.findByKeycloakId(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateProfile("invalid-keycloak-id", updates))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Користувача не знайдено");

        verify(userRepository).findByKeycloakId("invalid-keycloak-id");
        verify(userRepository, never()).save(any(User.class));
    }
}
