package com.microvolunteer.service;

import com.microvolunteer.dto.request.LoginRequest;
import com.microvolunteer.dto.request.RegisterRequest;
import com.microvolunteer.dto.response.AuthResponse;
import com.microvolunteer.entity.User;
import com.microvolunteer.enums.UserType;
import com.microvolunteer.exception.ValidationException;
import com.microvolunteer.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.ws.rs.core.Response;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тести для AuthService")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private Keycloak keycloak;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;
    private String testToken = "test-jwt-token";

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");
        registerRequest.setUserType(UserType.VOLUNTEER);

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

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
                .build();
    }

    @Test
    @DisplayName("Реєстрація користувача - успішно")
    void register_Success() {
        // Given
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        
        // Mock Keycloak
        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        
        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatus()).thenReturn(201);
        when(mockResponse.getLocation()).thenReturn(java.net.URI.create("http://localhost/users/keycloak-user-id"));
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(mockResponse);
        
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(anyString())).thenReturn(testToken);

        // When
        AuthResponse result = authService.register(registerRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo(testToken);
        assertThat(result.getUsername()).isEqualTo(testUser.getUsername());
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());

        verify(userRepository).existsByUsername(registerRequest.getUsername());
        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(usersResource).create(any(UserRepresentation.class));
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(anyString());
    }

    @Test
    @DisplayName("Реєстрація користувача - ім'я користувача вже існує")
    void register_UsernameExists() {
        // Given
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Користувач з таким ім'ям вже існує");

        verify(userRepository).existsByUsername(registerRequest.getUsername());
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(keycloak);
    }

    @Test
    @DisplayName("Реєстрація користувача - email вже існує")
    void register_EmailExists() {
        // Given
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Користувач з такою поштою вже існує");

        verify(userRepository).existsByUsername(registerRequest.getUsername());
        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(keycloak);
    }

    @Test
    @DisplayName("Вхід користувача - успішно")
    void login_Success() {
        // Given
        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(testUser.getKeycloakId())).thenReturn(testToken);

        // When
        AuthResponse result = authService.login(loginRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo(testToken);
        assertThat(result.getUsername()).isEqualTo(testUser.getUsername());
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());

        verify(userRepository).findByUsername(loginRequest.getUsername());
        verify(passwordEncoder).matches(loginRequest.getPassword(), testUser.getPassword());
        verify(jwtService).generateToken(testUser.getKeycloakId());
        verify(userRepository).save(testUser); // для оновлення lastLogin
    }

    @Test
    @DisplayName("Вхід користувача - користувач не знайдений")
    void login_UserNotFound() {
        // Given
        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Невірне ім'я користувача або пароль");

        verify(userRepository).findByUsername(loginRequest.getUsername());
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    @DisplayName("Вхід користувача - невірний пароль")
    void login_InvalidPassword() {
        // Given
        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Невірне ім'я користувача або пароль");

        verify(userRepository).findByUsername(loginRequest.getUsername());
        verify(passwordEncoder).matches(loginRequest.getPassword(), testUser.getPassword());
        verifyNoInteractions(jwtService);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Вхід користувача - неактивний користувач")
    void login_InactiveUser() {
        // Given
        testUser.setIsActive(false);
        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Акаунт деактивовано");

        verify(userRepository).findByUsername(loginRequest.getUsername());
        verify(passwordEncoder).matches(loginRequest.getPassword(), testUser.getPassword());
        verifyNoInteractions(jwtService);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Синхронізація з Keycloak - успішно")
    void syncWithKeycloak_Success() {
        // Given
        String keycloakId = "keycloak-user-id";
        UserRepresentation keycloakUser = new UserRepresentation();
        keycloakUser.setId(keycloakId);
        keycloakUser.setUsername("syncuser");
        keycloakUser.setEmail("sync@example.com");
        keycloakUser.setFirstName("Sync");
        keycloakUser.setLastName("User");

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());
        when(userRepository.existsByUsername(keycloakUser.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(keycloakUser.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = authService.syncUserWithKeycloak(keycloakUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testUser);

        verify(userRepository).findByKeycloakId(keycloakId);
        verify(userRepository).existsByUsername(keycloakUser.getUsername());
        verify(userRepository).existsByEmail(keycloakUser.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Синхронізація з Keycloak - користувач вже існує")
    void syncWithKeycloak_UserExists() {
        // Given
        String keycloakId = "keycloak-user-id";
        UserRepresentation keycloakUser = new UserRepresentation();
        keycloakUser.setId(keycloakId);

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(testUser));

        // When
        User result = authService.syncUserWithKeycloak(keycloakUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testUser);

        verify(userRepository).findByKeycloakId(keycloakId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Оновлення токену - успішно")
    void refreshToken_Success() {
        // Given
        String refreshToken = "refresh-token";
        String newAccessToken = "new-access-token";
        String keycloakId = "test-keycloak-id";

        when(jwtService.validateToken(refreshToken)).thenReturn(true);
        when(jwtService.getKeycloakIdFromToken(refreshToken)).thenReturn(keycloakId);
        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(keycloakId)).thenReturn(newAccessToken);

        // When
        AuthResponse result = authService.refreshToken(refreshToken);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo(newAccessToken);
        assertThat(result.getUsername()).isEqualTo(testUser.getUsername());

        verify(jwtService).validateToken(refreshToken);
        verify(jwtService).getKeycloakIdFromToken(refreshToken);
        verify(userRepository).findByKeycloakId(keycloakId);
        verify(jwtService).generateToken(keycloakId);
    }

    @Test
    @DisplayName("Оновлення токену - невалідний токен")
    void refreshToken_InvalidToken() {
        // Given
        String refreshToken = "invalid-refresh-token";

        when(jwtService.validateToken(refreshToken)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Невалідний refresh токен");

        verify(jwtService).validateToken(refreshToken);
        verifyNoMoreInteractions(jwtService);
        verifyNoInteractions(userRepository);
    }
}
