package com.microvolunteer.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, jwtExpiration * 7); // 7x довше для refresh token
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .claims(extraClaims)  // Оновлено метод
                .subject(userDetails.getUsername())  // Оновлено метод
                .issuedAt(new Date(System.currentTimeMillis()))  // Оновлено метод
                .expiration(new Date(System.currentTimeMillis() + expiration))  // Оновлено метод
                .signWith(getSignInKey())  // Видалено алгоритм - він визначається автоматично
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()  // Оновлено з parserBuilder
                .verifyWith(getSignInKey())  // Оновлено метод
                .build()
                .parseSignedClaims(token)  // Оновлено метод
                .getPayload();  // Оновлено метод
    }

    private SecretKey getSignInKey() {
        // Забезпечуємо мінімальну довжину ключа для HMAC-SHA256 (256 біт = 32 байти)
        String key = secretKey;
        if (key.length() < 32) {
            key = key + "0".repeat(32 - key.length()); // Доповнюємо нулями до мінімальної довжини
        }
        byte[] keyBytes = key.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
