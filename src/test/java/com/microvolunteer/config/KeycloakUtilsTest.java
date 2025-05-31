package com.microvolunteer.config;

import com.microvolunteer.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тести для KeycloakUtils")
class KeycloakUtilsTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private KeycloakUtils keycloakUtils;

    private final String testKeycloakId = "test-keycloak-id";
    private final String testUsername = "testuser";
    private final String testEmail = "test@example.com";
    private final String testFullName = "Test User";
    private final String testJwtToken = "valid.jwt.token";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Отримання Keycloak ID поточного користувача - успішно")
    void getCurrentUserKeycloakId_Success() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testKeycloakId);

        // When
        Optional<String> result = keycloakUtils.getCurrentUserKeycloakId();

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testKeycloakId);
    }

    @Test
    @DisplayName("Отримання Keycloak ID - неаутентифікований користувач")
    void getCurrentUserKeycloakId_NotAuthenticated() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When
        Optional<String> result = keycloakUtils.getCurrentUserKeycloakId();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Отримання Keycloak ID - неправильний тип principal")
    void getCurrentUserKeycloakId_WrongPrincipalType() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(new Object()); // Not String

        // When
        Optional<String> result = keycloakUtils.getCurrentUserKeycloakId();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Отримання email з JWT токена - успішно")
    void getCurrentUserEmail_Success() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + testJwtToken);
        
        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes)
                    .thenReturn(new ServletRequestAttributes(request));
            
            when(jwtService.extractEmail(testJwtToken)).thenReturn(testEmail);

            // When
            Optional<String> result = keycloakUtils.getCurrentUserEmail();

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(testEmail);
        }
    }

    @Test
    @DisplayName("Отримання email - немає Authorization заголовку")
    void getCurrentUserEmail_NoAuthHeader() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        
        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes)
                    .thenReturn(new ServletRequestAttributes(request));

            // When
            Optional<String> result = keycloakUtils.getCurrentUserEmail();

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Test
    @DisplayName("Отримання username з JWT токена - успішно")
    void getCurrentUsername_Success() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + testJwtToken);
        
        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes)
                    .thenReturn(new ServletRequestAttributes(request));
            
            when(jwtService.extractUsername(testJwtToken)).thenReturn(testUsername);

            // When
            Optional<String> result = keycloakUtils.getCurrentUsername();

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(testUsername);
        }
    }

    @Test
    @DisplayName("Отримання повного імені з JWT токена - успішно")
    void getCurrentUserFullName_Success() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + testJwtToken);
        
        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes)
                    .thenReturn(new ServletRequestAttributes(request));
            
            when(jwtService.extractFullName(testJwtToken)).thenReturn(testFullName);

            // When
            Optional<String> result = keycloakUtils.getCurrentUserFullName();

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(testFullName);
        }
    }

    @Test
    @DisplayName("Отримання ролей поточного користувача - успішно")
    void getCurrentUserRoles_Success() {
        // Given
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(authorities);

        // When
        List<String> result = keycloakUtils.getCurrentUserRoles();

        // Then
        assertThat(result).containsExactlyInAnyOrder("USER", "ADMIN");
    }

    @Test
    @DisplayName("Отримання ролей - неаутентифікований користувач")
    void getCurrentUserRoles_NotAuthenticated() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When
        List<String> result = keycloakUtils.getCurrentUserRoles();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Перевірка ролі - успішно")
    void hasRole_Success() {
        // Given
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(authorities);

        // When & Then
        assertThat(keycloakUtils.hasRole("USER")).isTrue();
        assertThat(keycloakUtils.hasRole("ADMIN")).isTrue();
        assertThat(keycloakUtils.hasRole("VOLUNTEER")).isFalse();
    }

    @Test
    @DisplayName("Перевірка будь-якої ролі - успішно")
    void hasAnyRole_Success() {
        // Given
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER")
        );
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(authorities);

        // When & Then
        assertThat(keycloakUtils.hasAnyRole("USER", "ADMIN")).isTrue();
        assertThat(keycloakUtils.hasAnyRole("ADMIN", "VOLUNTEER")).isFalse();
    }

    @Test
    @DisplayName("Перевірка аутентифікації - успішно")
    void isAuthenticated_Success() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testKeycloakId);

        // When
        boolean result = keycloakUtils.isAuthenticated();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Перевірка аутентифікації - неаутентифікований")
    void isAuthenticated_NotAuthenticated() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When
        boolean result = keycloakUtils.isAuthenticated();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Перевірка аутентифікації - анонімний користувач")
    void isAuthenticated_AnonymousUser() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        // When
        boolean result = keycloakUtils.isAuthenticated();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Отримання повної інформації про користувача - успішно")
    void getCurrentUserInfo_Success() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + testJwtToken);
        
        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes)
                    .thenReturn(new ServletRequestAttributes(request));
            
            when(jwtService.extractKeycloakId(testJwtToken)).thenReturn(testKeycloakId);
            when(jwtService.extractUsername(testJwtToken)).thenReturn(testUsername);
            when(jwtService.extractEmail(testJwtToken)).thenReturn(testEmail);
            when(jwtService.extractFullName(testJwtToken)).thenReturn(testFullName);
            when(jwtService.extractRoles(testJwtToken)).thenReturn(List.of("USER", "ADMIN"));

            // When
            Optional<KeycloakUtils.UserInfo> result = keycloakUtils.getCurrentUserInfo();

            // Then
            assertThat(result).isPresent();
            KeycloakUtils.UserInfo userInfo = result.get();
            assertThat(userInfo.getKeycloakId()).isEqualTo(testKeycloakId);
            assertThat(userInfo.getUsername()).isEqualTo(testUsername);
            assertThat(userInfo.getEmail()).isEqualTo(testEmail);
            assertThat(userInfo.getFullName()).isEqualTo(testFullName);
            assertThat(userInfo.getRoles()).containsExactlyInAnyOrder("USER", "ADMIN");
        }
    }

    @Test
    @DisplayName("Отримання JWT токена з запиту - успішно")
    void getCurrentToken_Success() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + testJwtToken);
        
        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes)
                    .thenReturn(new ServletRequestAttributes(request));

            // When
            Optional<String> result = keycloakUtils.getCurrentToken();

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(testJwtToken);
        }
    }
}