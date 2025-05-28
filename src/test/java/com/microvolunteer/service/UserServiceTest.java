package com.microvolunteer.service;

import com.microvolunteer.dto.request.UserCreateRequest;
import com.microvolunteer.dto.request.UserUpdateRequest;
import com.microvolunteer.dto.response.UserResponse;
import com.microvolunteer.entity.User;
import com.microvolunteer.enums.UserType;
import com.microvolunteer.exception.ResourceNotFoundException;
import com.microvolunteer.exception.ValidationException;
import com.microvolunteer.mapper.UserMapper;
import com.microvolunteer.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
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
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserResponse testUserResponse;
    private UserCreateRequest createRequest;
    private UserUpdateRequest updateRequest;

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
                .isActive(true)
                .dateJoined(LocalDateTime.now())
                .build();

        testUserResponse = new UserResponse();
        testUserResponse.setId(1L);
        testUserResponse.setUsername("testuser");
        testUserResponse.setEmail("test@example.com");
        testUserResponse.setFirstName("Test");
        testUserResponse.setLastName("User");
        testUserResponse.setUserType(UserType.VOLUNTEER.name());
        testUserResponse.setPhone("+380501234567");
        testUserResponse.setBio("Тестовий користувач");
        testUserResponse.setAddress("Київ, Україна");

        createRequest = new UserCreateRequest();
        createRequest.setUsername("newuser");
        createRequest.setEmail("newuser@example.com");
        createRequest.setPassword("password123");
        createRequest.setFirstName("New");
        createRequest.setLastName("User");
        createRequest.setUserType(UserType.VOLUNTEER);
        createRequest.setPhone("+380509876543");

        updateRequest = new UserUpdateRequest();
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("Name");
        updateRequest.setBio("Оновлена біографія");
        updateRequest.setAddress("Львів, Україна");
        updateRequest.setPhone("+380501111111");
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
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Користувача не знайдено");

        verify(userRepository).findByKeycloakId("invalid-keycloak-id");
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("Створення користувача - успішно")
    void createUser_Success() {
        // Given
        when(userRepository.existsByUsername(createRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(createRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(createRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // When
        UserResponse result = userService.createUser(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(testUserResponse.getUsername());

        verify(userRepository).existsByUsername(createRequest.getUsername());
        verify(userRepository).existsByEmail(createRequest.getEmail());
        verify(passwordEncoder).encode(createRequest.getPassword());
        verify(userRepository).save(any(User.class));
        verify(userMapper).toResponse(testUser);
    }

    @Test
    @DisplayName("Створення користувача - ім'я користувача вже існує")
    void createUser_UsernameExists() {
        // Given
        when(userRepository.existsByUsername(createRequest.getUsername())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(createRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Користувач з таким ім'ям вже існує");

        verify(userRepository).existsByUsername(createRequest.getUsername());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Створення користувача - email вже існує")
    void createUser_EmailExists() {
        // Given
        when(userRepository.existsByUsername(createRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(createRequest.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(createRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Користувач з такою поштою вже існує");

        verify(userRepository).existsByUsername(createRequest.getUsername());
        verify(userRepository).existsByEmail(createRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Оновлення користувача - успішно")
    void updateUser_Success() {
        // Given
        when(userRepository.findByKeycloakId(testUser.getKeycloakId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // When
        UserResponse result = userService.updateUser(testUser.getKeycloakId(), updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(testUser.getFirstName()).isEqualTo(updateRequest.getFirstName());
        assertThat(testUser.getLastName()).isEqualTo(updateRequest.getLastName());
        assertThat(testUser.getBio()).isEqualTo(updateRequest.getBio());
        assertThat(testUser.getAddress()).isEqualTo(updateRequest.getAddress());
        assertThat(testUser.getPhone()).isEqualTo(updateRequest.getPhone());

        verify(userRepository).findByKeycloakId(testUser.getKeycloakId());
        verify(userRepository).save(testUser);
        verify(userMapper).toResponse(testUser);
    }

    @Test
    @DisplayName("Оновлення користувача - не знайдено")
    void updateUser_NotFound() {
        // Given
        when(userRepository.findByKeycloakId(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUser("invalid-keycloak-id", updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Користувача не знайдено");

        verify(userRepository).findByKeycloakId("invalid-keycloak-id");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Отримання профілю - успішно")
    void getProfile_Success() {
        // Given
        when(userRepository.findByKeycloakId(testUser.getKeycloakId())).thenReturn(Optional.of(testUser));
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // When
        UserResponse result = userService.getProfile(testUser.getKeycloakId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUserResponse.getId());
        assertThat(result.getUsername()).isEqualTo(testUserResponse.getUsername());

        verify(userRepository).findByKeycloakId(testUser.getKeycloakId());
        verify(userMapper).toResponse(testUser);
    }

    @Test
    @DisplayName("Деактивація користувача - успішно")
    void deactivateUser_Success() {
        // Given
        testUser.setIsActive(true);
        when(userRepository.findByKeycloakId(testUser.getKeycloakId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.deactivateUser(testUser.getKeycloakId());

        // Then
        assertThat(testUser.getIsActive()).isFalse();

        verify(userRepository).findByKeycloakId(testUser.getKeycloakId());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Активація користувача - успішно")
    void activateUser_Success() {
        // Given
        testUser.setIsActive(false);
        when(userRepository.findByKeycloakId(testUser.getKeycloakId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.activateUser(testUser.getKeycloakId());

        // Then
        assertThat(testUser.getIsActive()).isTrue();

        verify(userRepository).findByKeycloakId(testUser.getKeycloakId());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Оновлення останнього входу - успішно")
    void updateLastLogin_Success() {
        // Given
        LocalDateTime beforeUpdate = testUser.getLastLogin();
        when(userRepository.findByKeycloakId(testUser.getKeycloakId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.updateLastLogin(testUser.getKeycloakId());

        // Then
        assertThat(testUser.getLastLogin()).isAfter(beforeUpdate == null ? LocalDateTime.MIN : beforeUpdate);

        verify(userRepository).findByKeycloakId(testUser.getKeycloakId());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Перевірка існування користувача за username")
    void existsByUsername() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        when(userRepository.existsByUsername("nonexistent")).thenReturn(false);

        // When & Then
        assertThat(userService.existsByUsername("testuser")).isTrue();
        assertThat(userService.existsByUsername("nonexistent")).isFalse();

        verify(userRepository, times(2)).existsByUsername(anyString());
    }

    @Test
    @DisplayName("Перевірка існування користувача за email")
    void existsByEmail() {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);

        // When & Then
        assertThat(userService.existsByEmail("test@example.com")).isTrue();
        assertThat(userService.existsByEmail("nonexistent@example.com")).isFalse();

        verify(userRepository, times(2)).existsByEmail(anyString());
    }
}
