package com.microvolunteer.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тести для JwtService (Keycloak версія)")
class JwtServiceTest {

    private JwtService jwtService;
    private final String testSecretKey = "microvolunteer-secret-key-that-should-be-at-least-256-bits-long-for-hmac-sha256";
    private final Long testExpiration = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", testSecretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", testExpiration);
    }

    @Test
    @DisplayName("Генерація внутрішнього токену - успішно")
    void generateInternalToken_Success() {
        // Given
        String keycloakId = "test-keycloak-id";
        String username = "testuser";
        String email = "test@example.com";

        // When
        String token = jwtService.generateInternalToken(keycloakId, username, email);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        // Перевірка що токен можна парсити
        SecretKey key = Keys.hmacShaKeyFor(testSecretKey.getBytes(StandardCharsets.UTF_8));
        var claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo(keycloakId);
        assertThat(claims.get("username", String.class)).isEqualTo(username);
        assertThat(claims.get("email", String.class)).isEqualTo(email);
        assertThat(claims.get("type", String.class)).isEqualTo("internal");
    }

    @Test
    @DisplayName("Витягування Keycloak ID з Keycloak токену")
    void extractKeycloakId_FromKeycloakToken() {
        // Given
        String keycloakId = "keycloak-user-123";
        String keycloakToken = createMockKeycloakToken(keycloakId, "testuser", "test@example.com");

        // When
        String extractedId = jwtService.extractKeycloakId(keycloakToken);

        // Then
        assertThat(extractedId).isEqualTo(keycloakId);
    }

    @Test
    @DisplayName("Витягування username з Keycloak токену - preferred_username")
    void extractUsername_PreferredUsername() {
        // Given
        String token = createMockKeycloakTokenWithPreferredUsername("keycloak-123", "preferred_user", "fallback_user");

        // When
        String username = jwtService.extractUsername(token);

        // Then
        assertThat(username).isEqualTo("preferred_user");
    }

    @Test
    @DisplayName("Витягування username з Keycloak токену - fallback до username")
    void extractUsername_FallbackToUsername() {
        // Given
        String token = createMockKeycloakTokenWithUsername("keycloak-123", "fallback_user");

        // When
        String username = jwtService.extractUsername(token);

        // Then
        assertThat(username).isEqualTo("fallback_user");
    }

    @Test
    @DisplayName("Витягування ролей з Keycloak токену - realm_access")
    void extractRoles_FromRealmAccess() {
        // Given
        String token = createMockKeycloakTokenWithRealmRoles("keycloak-123", List.of("USER", "ADMIN"));

        // When
        List<String> roles = jwtService.extractRoles(token);

        // Then
        assertThat(roles).containsExactlyInAnyOrder("USER", "ADMIN");
    }

    @Test
    @DisplayName("Витягування ролей з Keycloak токену - resource_access")
    void extractRoles_FromResourceAccess() {
        // Given
        String token = createMockKeycloakTokenWithResourceRoles("keycloak-123", "microvolunteer", List.of("USER", "VOLUNTEER"));

        // When
        List<String> roles = jwtService.extractRoles(token);

        // Then
        assertThat(roles).containsExactlyInAnyOrder("USER", "VOLUNTEER");
    }

    @Test
    @DisplayName("Витягування ролей - фільтрація системних ролей")
    void extractRoles_FilterSystemRoles() {
        // Given
        String token = createMockKeycloakTokenWithRealmRoles("keycloak-123", 
                List.of("USER", "offline_access", "uma_authorization", "ADMIN"));

        // When
        List<String> roles = jwtService.extractRoles(token);

        // Then
        assertThat(roles).containsExactlyInAnyOrder("USER", "ADMIN");
        assertThat(roles).doesNotContain("offline_access", "uma_authorization");
    }

    @Test
    @DisplayName("Витягування повного імені з Keycloak токену")
    void extractFullName_FromGivenAndFamilyName() {
        // Given
        String token = createMockKeycloakTokenWithNames("keycloak-123", "John", "Doe");

        // When
        String fullName = jwtService.extractFullName(token);

        // Then
        assertThat(fullName).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Витягування повного імені - тільки ім'я")
    void extractFullName_OnlyFirstName() {
        // Given
        String token = createMockKeycloakTokenWithNames("keycloak-123", "John", null);

        // When
        String fullName = jwtService.extractFullName(token);

        // Then
        assertThat(fullName).isEqualTo("John");
    }

    @Test
    @DisplayName("Витягування повного імені - fallback до name")
    void extractFullName_FallbackToName() {
        // Given
        String token = createMockKeycloakTokenWithNameClaim("keycloak-123", "John Doe");

        // When
        String fullName = jwtService.extractFullName(token);

        // Then
        assertThat(fullName).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Валідація Keycloak токену - успішно")
    void isTokenValid_ValidKeycloakToken() {
        // Given
        String token = createMockKeycloakToken("keycloak-123", "testuser", "test@example.com");

        // When
        boolean isValid = jwtService.isTokenValid(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Валідація токену без subject - false")
    void isTokenValid_NoSubject() {
        // Given
        String tokenWithoutSubject = createTokenWithoutSubject();

        // When
        boolean isValid = jwtService.isTokenValid(tokenWithoutSubject);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Валідація неvalidного токену - false")
    void isTokenValid_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtService.isTokenValid(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Валідація null/empty токенів")
    void isTokenValid_NullAndEmpty() {
        assertThat(jwtService.isTokenValid(null)).isFalse();
        assertThat(jwtService.isTokenValid("")).isFalse();
    }

    // Helper methods для створення mock Keycloak токенів

    private String createMockKeycloakToken(String keycloakId, String username, String email) {
        SecretKey key = Keys.hmacShaKeyFor(testSecretKey.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.builder()
                .subject(keycloakId)
                .claim("preferred_username", username)
                .claim("email", email)
                .claim("realm_access", Map.of("roles", List.of("USER")))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + testExpiration))
                .signWith(key)
                .compact();
    }

    private String createMockKeycloakTokenWithPreferredUsername(String keycloakId, String preferredUsername, String username) {
        SecretKey key = Keys.hmacShaKeyFor(testSecretKey.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.builder()
                .subject(keycloakId)
                .claim("preferred_username", preferredUsername)
                .claim("username", username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + testExpiration))
                .signWith(key)
                .compact();
    }

    private String createMockKeycloakTokenWithUsername(String keycloakId, String username) {
        SecretKey key = Keys.hmacShaKeyFor(testSecretKey.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.builder()
                .subject(keycloakId)
                .claim("username", username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + testExpiration))
                .signWith(key)
                .compact();
    }

    private String createMockKeycloakTokenWithRealmRoles(String keycloakId, List<String> roles) {
        SecretKey key = Keys.hmacShaKeyFor(testSecretKey.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.builder()
                .subject(keycloakId)
                .claim("realm_access", Map.of("roles", roles))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + testExpiration))
                .signWith(key)
                .compact();
    }

    private String createMockKeycloakTokenWithResourceRoles(String keycloakId, String clientId, List<String> roles) {
        SecretKey key = Keys.hmacShaKeyFor(testSecretKey.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.builder()
                .subject(keycloakId)
                .claim("resource_access", Map.of(clientId, Map.of("roles", roles)))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + testExpiration))
                .signWith(key)
                .compact();
    }

    private String createMockKeycloakTokenWithNames(String keycloakId, String firstName, String lastName) {
        SecretKey key = Keys.hmacShaKeyFor(testSecretKey.getBytes(StandardCharsets.UTF_8));
        
        var builder = Jwts.builder()
                .subject(keycloakId)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + testExpiration));
                
        if (firstName != null) {
            builder.claim("given_name", firstName);
        }
        if (lastName != null) {
            builder.claim("family_name", lastName);
        }
        
        return builder.signWith(key).compact();
    }

    private String createMockKeycloakTokenWithNameClaim(String keycloakId, String name) {
        SecretKey key = Keys.hmacShaKeyFor(testSecretKey.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.builder()
                .subject(keycloakId)
                .claim("name", name)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + testExpiration))
                .signWith(key)
                .compact();
    }

    private String createTokenWithoutSubject() {
        SecretKey key = Keys.hmacShaKeyFor(testSecretKey.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.builder()
                .claim("username", "testuser")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + testExpiration))
                .signWith(key)
                .compact();
    }
}