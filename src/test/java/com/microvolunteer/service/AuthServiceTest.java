package com.microvolunteer.service;

import com.microvolunteer.dto.request.UserRegistrationRequest;
import com.microvolunteer.dto.response.UserResponse;
import com.microvolunteer.entity.User;
import com.microvolunteer.enums.UserType;
import com.microvolunteer.exception.BusinessException;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тести для AuthService")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private UserResponse testUserResponse;
    private UserRegistrationRequest registrationRequest;

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
                .isActive(true)
                .dateJoined(LocalDateTime.now())
                .build();

        registrationRequest = UserRegistrationRequest.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("password123")
                .firstName("New")
                .lastName("User")
                .userType(UserType.VOLUNTEER)
                .build();

        // Встановлення значень для тестування
        ReflectionTestUtils.setField(authService, "keycloakServerUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(authService, "realm", "microvolunteer");
        ReflectionTestUtils.setField(authService, "adminUsername", "admin");
        ReflectionTestUtils.setField(authService, "adminPassword", "admin");
    }

    @Test
    @DisplayName("Реєстрація користувача - username вже існує")
    void register_UsernameExists() {
        // Given
        when(userRepository.existsByUsername(registrationRequest.getUsername())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(registrationRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Користувач з таким ім'ям вже існує");

        verify(userRepository).existsByUsername(registrationRequest.getUsername());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Реєстрація користувача - email вже існує")
    void register_EmailExists() {
        // Given
        when(userRepository.existsByUsername(registrationRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registrationRequest.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(registrationRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Користувач з таким email вже існує");

        verify(userRepository).existsByUsername(registrationRequest.getUsername());
        verify(userRepository).existsByEmail(registrationRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Синхронізація користувача з Keycloak - новий користувач")
    void syncKeycloakUser_NewUser() {
        // Given
        String keycloakId = "new-keycloak-id";
        String username = "newuser";
        String email = "newuser@example.com";

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());
        when(passwordEncoder.encode("temp-password")).thenReturn("encodedTempPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // When
        UserResponse result = authService.syncKeycloakUser(keycloakId, username, email);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(testUserResponse.getUsername());

        verify(userRepository).findByKeycloakId(keycloakId);
        verify(passwordEncoder).encode("temp-password");
        verify(userRepository).save(any(User.class));
        verify(userMapper).toResponse(testUser);
    }

    @Test
    @DisplayName("Синхронізація користувача з Keycloak - існуючий користувач")
    void syncKeycloakUser_ExistingUser() {
        // Given
        String keycloakId = "existing-keycloak-id";
        String username = "updateduser";
        String email = "updated@example.com";

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // When
        UserResponse result = authService.syncKeycloakUser(keycloakId, username, email);

        // Then
        assertThat(result).isNotNull();
        assertThat(testUser.getUsername()).isEqualTo(username);
        assertThat(testUser.getEmail()).isEqualTo(email);
        assertThat(testUser.getLastLogin()).isNotNull();

        verify(userRepository).findByKeycloakId(keycloakId);
        verify(userRepository).save(testUser);
        verify(userMapper).toResponse(testUser);
    }

    @Test
    @DisplayName("Генерація токену - успішно")
    void generateToken_Success() {
        // Given
        String keycloakId = "test-keycloak-id";
        String expectedToken = "jwt.token.here";

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(keycloakId, testUser.getUsername(), testUser.getEmail()))
                .thenReturn(expectedToken);

        // When
        String result = authService.generateToken(keycloakId);

        // Then
        assertThat(result).isEqualTo(expectedToken);

        verify(userRepository).findByKeycloakId(keycloakId);
        verify(jwtService).generateToken(keycloakId, testUser.getUsername(), testUser.getEmail());
    }

    @Test
    @DisplayName("Генерація токену - користувач не знайдений")
    void generateToken_UserNotFound() {
        // Given
        String keycloakId = "invalid-keycloak-id";
        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.generateToken(keycloakId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Користувача не знайдено");

        verify(userRepository).findByKeycloakId(keycloakId);
        verifyNoInteractions(jwtService);
    }
}
