package com.microvolunteer.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тести для JwtService")
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private String secret = "microvolunteer-secret-key-that-should-be-at-least-256-bits-long-for-security";
    private long expiration = 86400000; // 24 години
    private String testKeycloakId = "test-keycloak-id";
    private SecretKey key;

    @BeforeEach
    void setUp() {
        // Встановлення значень через reflection
        ReflectionTestUtils.setField(jwtService, "secret", secret);
        ReflectionTestUtils.setField(jwtService, "expiration", expiration);
        
        // Ініціалізація ключа
        key = Keys.hmacShaKeyFor(secret.getBytes());
        ReflectionTestUtils.setField(jwtService, "key", key);
    }

    @Test
    @DisplayName("Генерація токену - успішно")
    void generateToken_Success() {
        // When
        String token = jwtService.generateToken(testKeycloakId);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
        
        // Перевірка вмісту токену
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
                
        assertThat(claims.getSubject()).isEqualTo(testKeycloakId);
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    @Test
    @DisplayName("Отримання Keycloak ID з токену - успішно")
    void getKeycloakIdFromToken_Success() {
        // Given
        String token = jwtService.generateToken(testKeycloakId);

        // When
        String extractedId = jwtService.getKeycloakIdFromToken(token);

        // Then
        assertThat(extractedId).isNotNull();
        assertThat(extractedId).isEqualTo(testKeycloakId);
    }

    @Test
    @DisplayName("Отримання Keycloak ID з невалідного токену")
    void getKeycloakIdFromToken_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        assertThatThrownBy(() -> jwtService.getKeycloakIdFromToken(invalidToken))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Валідація токену - валідний токен")
    void validateToken_ValidToken() {
        // Given
        String token = jwtService.generateToken(testKeycloakId);

        // When
        boolean isValid = jwtService.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Валідація токену - невалідний токен")
    void validateToken_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtService.validateToken(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Валідація токену - прострочений токен")
    void validateToken_ExpiredToken() {
        // Given
        Date past = new Date(System.currentTimeMillis() - 1000);
        String expiredToken = Jwts.builder()
                .setSubject(testKeycloakId)
                .setIssuedAt(new Date(System.currentTimeMillis() - 2000))
                .setExpiration(past)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // When
        boolean isValid = jwtService.validateToken(expiredToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Валідація токену - токен з невірним підписом")
    void validateToken_WrongSignature() {
        // Given
        SecretKey wrongKey = Keys.hmacShaKeyFor("wrong-secret-key-for-testing-purposes-only-32bytes".getBytes());
        String tokenWithWrongSignature = Jwts.builder()
                .setSubject(testKeycloakId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(wrongKey, SignatureAlgorithm.HS256)
                .compact();

        // When
        boolean isValid = jwtService.validateToken(tokenWithWrongSignature);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Валідація токену - null токен")
    void validateToken_NullToken() {
        // When
        boolean isValid = jwtService.validateToken(null);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Валідація токену - порожній токен")
    void validateToken_EmptyToken() {
        // When
        boolean isValid = jwtService.validateToken("");

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Отримання часу закінчення токену - успішно")
    void getExpirationDateFromToken_Success() {
        // Given
        String token = jwtService.generateToken(testKeycloakId);

        // When
        Date expirationDate = jwtService.getExpirationDateFromToken(token);

        // Then
        assertThat(expirationDate).isNotNull();
        assertThat(expirationDate).isAfter(new Date());
        assertThat(expirationDate.getTime() - System.currentTimeMillis()).isLessThanOrEqualTo(expiration);
    }

    @Test
    @DisplayName("Перевірка чи токен прострочений - не прострочений")
    void isTokenExpired_NotExpired() {
        // Given
        String token = jwtService.generateToken(testKeycloakId);

        // When
        boolean isExpired = jwtService.isTokenExpired(token);

        // Then
        assertThat(isExpired).isFalse();
    }

    @Test
    @DisplayName("Перевірка чи токен прострочений - прострочений")
    void isTokenExpired_Expired() {
        // Given
        Date past = new Date(System.currentTimeMillis() - 1000);
        String expiredToken = Jwts.builder()
                .setSubject(testKeycloakId)
                .setIssuedAt(new Date(System.currentTimeMillis() - 2000))
                .setExpiration(past)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // When
        boolean isExpired = jwtService.isTokenExpired(expiredToken);

        // Then
        assertThat(isExpired).isTrue();
    }

    @Test
    @DisplayName("Генерація refresh токену")
    void generateRefreshToken_Success() {
        // When
        String refreshToken = jwtService.generateRefreshToken(testKeycloakId);

        // Then
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotBlank();
        
        // Перевірка що refresh токен має довший термін дії
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();
                
        assertThat(claims.getSubject()).isEqualTo(testKeycloakId);
        
        // Refresh токен має діяти 7 днів
        long sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000L;
        assertThat(claims.getExpiration().getTime() - claims.getIssuedAt().getTime())
                .isGreaterThanOrEqualTo(sevenDaysInMillis - 1000); // -1000 для допустимої похибки
    }
}
