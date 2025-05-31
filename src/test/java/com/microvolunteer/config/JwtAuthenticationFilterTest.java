package com.microvolunteer.config;

import com.microvolunteer.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тести для JwtAuthenticationFilter")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private final String validToken = "valid.jwt.token";
    private final String keycloakId = "test-keycloak-id";

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        
        // Reset SecurityContext
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Валідний JWT токен - успішна аутентифікація")
    void doFilterInternal_ValidToken_Success() throws ServletException, IOException {
        // Given
        request.addHeader("Authorization", "Bearer " + validToken);
        
        when(jwtService.isTokenValid(validToken)).thenReturn(true);
        when(jwtService.extractKeycloakId(validToken)).thenReturn(keycloakId);
        when(jwtService.extractRoles(validToken)).thenReturn(List.of("USER", "ADMIN"));

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            when(SecurityContextHolder.getContext()).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);
            
            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(jwtService).isTokenValid(validToken);
            verify(jwtService).extractKeycloakId(validToken);
            verify(jwtService).extractRoles(validToken);
            verify(securityContext).setAuthentication(any(Authentication.class));
            verify(filterChain).doFilter(request, response);
        }
    }

    @Test
    @DisplayName("Валідний токен з порожніми ролями - додається базова роль USER")
    void doFilterInternal_ValidTokenEmptyRoles_AddsUserRole() throws ServletException, IOException {
        // Given
        request.addHeader("Authorization", "Bearer " + validToken);
        
        when(jwtService.isTokenValid(validToken)).thenReturn(true);
        when(jwtService.extractKeycloakId(validToken)).thenReturn(keycloakId);
        when(jwtService.extractRoles(validToken)).thenReturn(List.of()); // Порожній список ролей

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            when(SecurityContextHolder.getContext()).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);
            
            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(securityContext).setAuthentication(argThat(auth -> {
                boolean hasUserRole = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER"));
                assertThat(hasUserRole).isTrue();
                assertThat(auth.getAuthorities()).hasSize(1);
                return true;
            }));
            verify(filterChain).doFilter(request, response);
        }
    }

    @Test
    @DisplayName("Немає Authorization заголовку - продовжує без аутентифікації")
    void doFilterInternal_NoAuthHeader_ContinuesWithoutAuth() throws ServletException, IOException {
        // Given
        // Немає Authorization заголовку

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verifyNoInteractions(jwtService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Неправильний формат Authorization заголовку - продовжує без аутентифікації")
    void doFilterInternal_InvalidAuthHeader_ContinuesWithoutAuth() throws ServletException, IOException {
        // Given
        request.addHeader("Authorization", "Basic username:password");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verifyNoInteractions(jwtService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Невалідний JWT токен - продовжує без аутентифікації")
    void doFilterInternal_InvalidToken_ContinuesWithoutAuth() throws ServletException, IOException {
        // Given
        String invalidToken = "invalid.jwt.token";
        request.addHeader("Authorization", "Bearer " + invalidToken);
        
        when(jwtService.isTokenValid(invalidToken)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtService).isTokenValid(invalidToken);
        verifyNoMoreInteractions(jwtService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("JWT токен без Keycloak ID - продовжує без аутентифікації")
    void doFilterInternal_TokenWithoutKeycloakId_ContinuesWithoutAuth() throws ServletException, IOException {
        // Given
        request.addHeader("Authorization", "Bearer " + validToken);
        
        when(jwtService.isTokenValid(validToken)).thenReturn(true);
        when(jwtService.extractKeycloakId(validToken)).thenReturn(null); // Немає Keycloak ID

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            when(SecurityContextHolder.getContext()).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);
            
            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(jwtService).isTokenValid(validToken);
            verify(jwtService).extractKeycloakId(validToken);
            verify(securityContext, never()).setAuthentication(any());
            verify(filterChain).doFilter(request, response);
        }
    }

    @Test
    @DisplayName("Користувач вже аутентифікований - не перезаписує аутентифікацію")
    void doFilterInternal_AlreadyAuthenticated_DoesNotOverride() throws ServletException, IOException {
        // Given
        request.addHeader("Authorization", "Bearer " + validToken);
        Authentication existingAuth = mock(Authentication.class);
        
        when(jwtService.isTokenValid(validToken)).thenReturn(true);
        when(jwtService.extractKeycloakId(validToken)).thenReturn(keycloakId);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            when(SecurityContextHolder.getContext()).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(existingAuth); // Вже аутентифікований
            
            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(jwtService).isTokenValid(validToken);
            verify(jwtService).extractKeycloakId(validToken);
            verify(securityContext, never()).setAuthentication(any());
            verify(filterChain).doFilter(request, response);
        }
    }

    @Test
    @DisplayName("Виняток при парсингу токена - продовжує без аутентифікації")
    void doFilterInternal_TokenParsingException_ContinuesWithoutAuth() throws ServletException, IOException {
        // Given
        request.addHeader("Authorization", "Bearer " + validToken);
        
        when(jwtService.isTokenValid(validToken)).thenThrow(new RuntimeException("Token parsing error"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtService).isTokenValid(validToken);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Ролі конвертуються правильно у Spring Security authorities")
    void doFilterInternal_RolesConvertedCorrectly() throws ServletException, IOException {
        // Given
        request.addHeader("Authorization", "Bearer " + validToken);
        List<String> keycloakRoles = List.of("user", "admin", "volunteer");
        
        when(jwtService.isTokenValid(validToken)).thenReturn(true);
        when(jwtService.extractKeycloakId(validToken)).thenReturn(keycloakId);
        when(jwtService.extractRoles(validToken)).thenReturn(keycloakRoles);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            when(SecurityContextHolder.getContext()).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);
            
            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(securityContext).setAuthentication(argThat(auth -> {
                assertThat(auth.getAuthorities()).containsExactlyInAnyOrder(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_VOLUNTEER")
                );
                assertThat(auth.getPrincipal()).isEqualTo(keycloakId);
                assertThat(auth.getCredentials()).isNull();
                return true;
            }));
            verify(filterChain).doFilter(request, response);
        }
    }

    @Test
    @DisplayName("Порожній Bearer токен - продовжує без аутентифікації")
    void doFilterInternal_EmptyBearerToken_ContinuesWithoutAuth() throws ServletException, IOException {
        // Given
        request.addHeader("Authorization", "Bearer ");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verifyNoInteractions(jwtService);
        verify(filterChain).doFilter(request, response);
    }
}