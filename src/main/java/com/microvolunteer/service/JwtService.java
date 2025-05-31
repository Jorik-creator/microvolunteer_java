package com.microvolunteer.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {

    @Value("${app.jwt.secret:microvolunteer-secret-key-that-should-be-at-least-256-bits-long}")
    private String secretKey;

    @Value("${app.jwt.expiration:86400000}") // 24 hours
    private Long jwtExpiration;

    /**
     * Витягує Keycloak ID з токена (subject claim)
     */
    public String extractKeycloakId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Витягує дату закінчення токена
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Витягує username з Keycloak токена
     */
    public String extractUsername(String token) {
        // Спочатку спробуємо preferred_username (стандарт Keycloak)
        Claims claims = extractAllClaims(token);
        String username = claims.get("preferred_username", String.class);
        
        // Якщо немає, спробуємо username
        if (username == null) {
            username = claims.get("username", String.class);
        }
        
        // Якщо немає, використаємо subject як fallback
        if (username == null) {
            username = claims.getSubject();
        }
        
        return username;
    }

    /**
     * Витягує email з Keycloak токена
     */
    public String extractEmail(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("email", String.class);
    }

    /**
     * Витягує ролі з Keycloak токена
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        
        // Спробуємо витягти ролі з різних можливих місць в Keycloak токені
        List<String> roles = new ArrayList<>();
        
        // 1. Ролі з realm_access
        Map<String, Object> realmAccess = claims.get("realm_access", Map.class);
        if (realmAccess != null && realmAccess.get("roles") instanceof List) {
            List<String> realmRoles = (List<String>) realmAccess.get("roles");
            roles.addAll(realmRoles);
        }
        
        // 2. Ролі з resource_access для нашого клієнта
        Map<String, Object> resourceAccess = claims.get("resource_access", Map.class);
        if (resourceAccess != null) {
            // Шукаємо ролі для клієнта microvolunteer або account
            for (String clientId : Arrays.asList("microvolunteer", "account")) {
                Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(clientId);
                if (clientAccess != null && clientAccess.get("roles") instanceof List) {
                    List<String> clientRoles = (List<String>) clientAccess.get("roles");
                    roles.addAll(clientRoles);
                }
            }
        }
        
        // 3. Прямі ролі (якщо вони є)
        Object directRoles = claims.get("roles");
        if (directRoles instanceof List) {
            roles.addAll((List<String>) directRoles);
        }
        
        // Видаляємо дублікати і системні ролі
        return roles.stream()
                .distinct()
                .filter(role -> !role.equals("offline_access") && !role.equals("uma_authorization"))
                .toList();
    }

    /**
     * Витягує повне ім'я користувача
     */
    public String extractFullName(String token) {
        Claims claims = extractAllClaims(token);
        String firstName = claims.get("given_name", String.class);
        String lastName = claims.get("family_name", String.class);
        
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        
        // Fallback до name або username
        String name = claims.get("name", String.class);
        if (name != null) {
            return name;
        }
        
        return extractUsername(token);
    }

    /**
     * Перевіряє валідність токена
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !isTokenExpired(token) && claims.getSubject() != null;
        } catch (MalformedJwtException e) {
            log.error("Невалідний JWT токен: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Помилка валідації JWT токена: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Генерує внутрішній JWT токен для інтеграції
     * (використовується тільки для внутрішніх потреб, якщо необхідно)
     */
    public String generateInternalToken(String keycloakId, String username, String email) {
        return Jwts.builder()
                .subject(keycloakId)
                .claim("username", username)
                .claim("email", email)
                .claim("type", "internal")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Витягує конкретний claim з токена
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        return expiration != null && expiration.before(new Date());
    }

    private Claims extractAllClaims(String token) {
        try {
            // Спочатку спробуємо парсити як підписаний токен
            return Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            // Якщо не вдалося з нашим ключем, спробуємо парсити як неперевірений
            // (для Keycloak токенів з іншим підписом)
            try {
                return Jwts.parser()
                        .build()
                        .parseUnsecuredClaims(token)
                        .getPayload();
            } catch (Exception ex) {
                log.error("Не вдалося парсити JWT токен: {}", ex.getMessage());
                throw new RuntimeException("Невалідний JWT токен", ex);
            }
        }
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}