package com.microvolunteer.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тести для JwtService")
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
    @DisplayName("Генерація токену - успішно")
    void generateToken_Success() {
        // Given
        String keycloakId = "test-keycloak-id";
        String username = "testuser";
        String email = "test@example.com";

        // When
        String token = jwtService.generateToken(keycloakId, username, email);

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
    }

    @Test
    @DisplayName("Витягування Keycloak ID з токену - успішно")
    void extractKeycloakId_Success() {
        // Given
        String keycloakId = "test-keycloak-id";
        String token = jwtService.generateToken(keycloakId, "testuser", "test@example.com");

        // When
        String extractedId = jwtService.extractKeycloakId(token);

        // Then
        assertThat(extractedId).isEqualTo(keycloakId);
    }

    @Test
    @DisplayName("Витягування Keycloak ID з неvalidного токену - помилка")
    void extractKeycloakId_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        assertThatThrownBy(() -> jwtService.extractKeycloakId(invalidToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Валідація токену - успішно")
    void isTokenValid_Success() {
        // Given
        String token = jwtService.generateToken("test-keycloak-id", "testuser", "test@example.com");

        // When
        boolean isValid = jwtService.isTokenValid(token);

        // Then
        assertThat(isValid).isTrue();
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
    @DisplayName("Валідація expired токену - false")
    void isTokenValid_ExpiredToken() {
        // Given
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1000L); // Expired immediately
        String expiredToken = jwtService.generateToken("test-keycloak-id", "testuser", "test@example.com");

        // Reset expiration
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", testExpiration);

        // When
        boolean isValid = jwtService.isTokenValid(expiredToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Валідація null токену - false")
    void isTokenValid_NullToken() {
        // When
        boolean isValid = jwtService.isTokenValid(null);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Валідація порожнього токену - false")
    void isTokenValid_EmptyToken() {
        // When
        boolean isValid = jwtService.isTokenValid("");

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Витягування дати закінчення з токену - успішно")
    void extractExpiration_Success() {
        // Given
        String token = jwtService.generateToken("test-keycloak-id", "testuser", "test@example.com");

        // When
        Date expiration = jwtService.extractExpiration(token);

        // Then
        assertThat(expiration).isNotNull();
        assertThat(expiration).isAfter(new Date());
    }

    @Test
    @DisplayName("Витягування username з токену - успішно")
    void extractUsername_Success() {
        // Given
        String username = "testuser";
        String token = jwtService.generateToken("test-keycloak-id", username, "test@example.com");

        // When
        String extractedUsername = jwtService.extractUsername(token);

        // Then
        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    @DisplayName("Витягування email з токену - успішно")
    void extractEmail_Success() {
        // Given
        String email = "test@example.com";
        String token = jwtService.generateToken("test-keycloak-id", "testuser", email);

        // When
        String extractedEmail = jwtService.extractEmail(token);

        // Then
        assertThat(extractedEmail).isEqualTo(email);
    }
}
