package com.microvolunteer.service;

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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тести для AuthService (Keycloak версія)")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private UserResponse testUserResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .keycloakId("test-keycloak-id")
                .username("testuser")
                .email("test@example.com")
                .password("") // Порожній пароль для Keycloak
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
    }

    @Test
    @DisplayName("Синхронізація користувача з Keycloak - новий користувач")
    void syncKeycloakUser_NewUser() {
        // Given
        String keycloakId = "new-keycloak-id";
        String username = "newuser";
        String email = "newuser@example.com";

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // When
        UserResponse result = authService.syncKeycloakUser(keycloakId, username, email);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(testUserResponse.getUsername());

        verify(userRepository).findByKeycloakId(keycloakId);
        verify(userRepository).save(any(User.class));
        verify(userMapper).toResponse(testUser);
    }

    @Test
    @DisplayName("Синхронізація користувача з Keycloak - новий користувач з email")
    void syncKeycloakUser_NewUserWithEmailParsing() {
        // Given
        String keycloakId = "new-keycloak-id";
        String username = "testuser";
        String email = "john.doe@example.com";

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            // Перевірка що ім'я було витягнуто з email
            assertThat(savedUser.getFirstName()).isEqualTo("John");
            assertThat(savedUser.getLastName()).isEqualTo("Doe");
            return savedUser;
        });
        when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

        // When
        UserResponse result = authService.syncKeycloakUser(keycloakId, username, email);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).findByKeycloakId(keycloakId);
        verify(userRepository).save(any(User.class));
        verify(userMapper).toResponse(any(User.class));
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
    @DisplayName("Синхронізація з JWT токена - новий користувач")
    void syncFromJwtToken_NewUser() {
        // Given
        String jwtToken = "valid.jwt.token";
        String keycloakId = "jwt-keycloak-id";
        String username = "jwtuser";
        String email = "jwt@example.com";
        String fullName = "JWT User";

        when(jwtService.extractKeycloakId(jwtToken)).thenReturn(keycloakId);
        when(jwtService.extractUsername(jwtToken)).thenReturn(username);
        when(jwtService.extractEmail(jwtToken)).thenReturn(email);
        when(jwtService.extractFullName(jwtToken)).thenReturn(fullName);
        
        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // When
        UserResponse result = authService.syncFromJwtToken(jwtToken);

        // Then
        assertThat(result).isNotNull();
        verify(jwtService).extractKeycloakId(jwtToken);
        verify(jwtService).extractUsername(jwtToken);
        verify(jwtService).extractEmail(jwtToken);
        verify(jwtService).extractFullName(jwtToken);
        verify(userRepository).findByKeycloakId(keycloakId);
        verify(userRepository).save(any(User.class));
        verify(userMapper).toResponse(testUser);
    }

    @Test
    @DisplayName("Синхронізація з JWT токена - помилка парсингу")
    void syncFromJwtToken_ParseError() {
        // Given
        String invalidToken = "invalid.jwt.token";
        when(jwtService.extractKeycloakId(invalidToken)).thenThrow(new RuntimeException("Invalid token"));

        // When & Then
        assertThatThrownBy(() -> authService.syncFromJwtToken(invalidToken))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Не вдалося синхронізувати користувача з Keycloak");

        verify(jwtService).extractKeycloakId(invalidToken);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Генерація внутрішнього токену - успішно")
    void generateInternalToken_Success() {
        // Given
        String keycloakId = "test-keycloak-id";
        String expectedToken = "internal.jwt.token";

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(testUser));
        when(jwtService.generateInternalToken(keycloakId, testUser.getUsername(), testUser.getEmail()))
                .thenReturn(expectedToken);

        // When
        String result = authService.generateInternalToken(keycloakId);

        // Then
        assertThat(result).isEqualTo(expectedToken);
        verify(userRepository).findByKeycloakId(keycloakId);
        verify(jwtService).generateInternalToken(keycloakId, testUser.getUsername(), testUser.getEmail());
    }

    @Test
    @DisplayName("Генерація внутрішнього токену - користувач не знайдений")
    void generateInternalToken_UserNotFound() {
        // Given
        String keycloakId = "invalid-keycloak-id";
        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.generateInternalToken(keycloakId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Користувача не знайдено");

        verify(userRepository).findByKeycloakId(keycloakId);
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("Отримання користувача за Keycloak ID - успішно")
    void getUserByKeycloakId_Success() {
        // Given
        String keycloakId = "test-keycloak-id";
        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(testUser));
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // When
        UserResponse result = authService.getUserByKeycloakId(keycloakId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(testUserResponse.getUsername());
        verify(userRepository).findByKeycloakId(keycloakId);
        verify(userMapper).toResponse(testUser);
    }

    @Test
    @DisplayName("Отримання користувача за Keycloak ID - не знайдено")
    void getUserByKeycloakId_NotFound() {
        // Given
        String keycloakId = "invalid-keycloak-id";
        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.getUserByKeycloakId(keycloakId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Користувача не знайдено");

        verify(userRepository).findByKeycloakId(keycloakId);
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("Синхронізація користувача - без змін даних")
    void syncKeycloakUser_NoChanges() {
        // Given
        String keycloakId = "existing-keycloak-id";
        String username = testUser.getUsername(); // Той самий username
        String email = testUser.getEmail(); // Той самий email

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // When
        UserResponse result = authService.syncKeycloakUser(keycloakId, username, email);

        // Then
        assertThat(result).isNotNull();
        // Перевіряємо що lastLogin все одно оновився
        assertThat(testUser.getLastLogin()).isNotNull();

        verify(userRepository).findByKeycloakId(keycloakId);
        verify(userRepository).save(testUser);
        verify(userMapper).toResponse(testUser);
    }
}