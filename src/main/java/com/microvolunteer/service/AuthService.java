package com.microvolunteer.service;

import com.microvolunteer.dto.response.UserResponse;
import com.microvolunteer.entity.User;
import com.microvolunteer.exception.BusinessException;
import com.microvolunteer.mapper.UserMapper;
import com.microvolunteer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtService jwtService;

    /**
     * Синхронізація користувача з Keycloak
     * Створює або оновлює локального користувача на основі даних з Keycloak токена
     */
    @Transactional
    public UserResponse syncKeycloakUser(String keycloakId, String username, String email) {
        log.info("Синхронізація користувача з Keycloak ID: {}", keycloakId);

        User user = userRepository.findByKeycloakId(keycloakId).orElse(null);

        if (user == null) {
            // Створення нового користувача
            log.info("Створення нового користувача для Keycloak ID: {}", keycloakId);
            
            user = User.builder()
                    .keycloakId(keycloakId)
                    .username(username)
                    .email(email)
                    .password("") // Пароль керується в Keycloak
                    .isActive(true)
                    .dateJoined(LocalDateTime.now())
                    .build();
                    
            // Спробуємо витягти додаткову інформацію з email
            if (email != null && email.contains("@")) {
                String[] emailParts = email.split("@");
                if (emailParts[0].contains(".")) {
                    String[] nameParts = emailParts[0].split("\\.");
                    if (nameParts.length >= 2) {
                        user.setFirstName(capitalize(nameParts[0]));
                        user.setLastName(capitalize(nameParts[1]));
                    }
                }
            }
        } else {
            // Оновлення існуючого користувача
            log.info("Оновлення існуючого користувача: {}", user.getUsername());
            
            boolean updated = false;
            
            if (username != null && !username.equals(user.getUsername())) {
                user.setUsername(username);
                updated = true;
            }
            
            if (email != null && !email.equals(user.getEmail())) {
                user.setEmail(email);
                updated = true;
            }
            
            // Завжди оновлюємо час останнього входу
            user.setLastLogin(LocalDateTime.now());
            
            if (updated) {
                log.info("Оновлено дані користувача: {}", username);
            }
        }

        User savedUser = userRepository.save(user);
        log.info("Користувач {} успішно синхронізований", savedUser.getUsername());
        
        return userMapper.toResponse(savedUser);
    }

    /**
     * Синхронізація користувача з повною інформацією з JWT токена
     */
    @Transactional
    public UserResponse syncFromJwtToken(String jwtToken) {
        try {
            String keycloakId = jwtService.extractKeycloakId(jwtToken);
            String username = jwtService.extractUsername(jwtToken);
            String email = jwtService.extractEmail(jwtToken);
            String fullName = jwtService.extractFullName(jwtToken);
            
            log.info("Синхронізація користувача з JWT токена: keycloakId={}, username={}", keycloakId, username);
            
            User user = userRepository.findByKeycloakId(keycloakId).orElse(null);

            if (user == null) {
                // Створення нового користувача з повною інформацією
                user = User.builder()
                        .keycloakId(keycloakId)
                        .username(username)
                        .email(email)
                        .password("") // Пароль керується в Keycloak
                        .isActive(true)
                        .dateJoined(LocalDateTime.now())
                        .build();
                        
                // Парсимо повне ім'я
                if (fullName != null && fullName.contains(" ")) {
                    String[] nameParts = fullName.split(" ", 2);
                    user.setFirstName(nameParts[0]);
                    user.setLastName(nameParts[1]);
                } else if (fullName != null) {
                    user.setFirstName(fullName);
                }
                
                log.info("Створено нового користувача: {}", username);
            } else {
                // Оновлення існуючого користувача
                boolean updated = false;
                
                if (username != null && !username.equals(user.getUsername())) {
                    user.setUsername(username);
                    updated = true;
                }
                
                if (email != null && !email.equals(user.getEmail())) {
                    user.setEmail(email);
                    updated = true;
                }
                
                // Оновлюємо ім'я, якщо воно змінилося в Keycloak
                if (fullName != null && fullName.contains(" ")) {
                    String[] nameParts = fullName.split(" ", 2);
                    if (!nameParts[0].equals(user.getFirstName()) || !nameParts[1].equals(user.getLastName())) {
                        user.setFirstName(nameParts[0]);
                        user.setLastName(nameParts[1]);
                        updated = true;
                    }
                }
                
                user.setLastLogin(LocalDateTime.now());
                
                if (updated) {
                    log.info("Оновлено дані користувача: {}", username);
                }
            }

            User savedUser = userRepository.save(user);
            return userMapper.toResponse(savedUser);
            
        } catch (Exception e) {
            log.error("Помилка синхронізації користувача з JWT токена: {}", e.getMessage());
            throw BusinessException.badRequest("Не вдалося синхронізувати користувача з Keycloak");
        }
    }

    /**
     * Генерує внутрішній токен для користувача (якщо потрібно)
     */
    public String generateInternalToken(String keycloakId) {
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> BusinessException.notFound("Користувача не знайдено"));

        return jwtService.generateInternalToken(keycloakId, user.getUsername(), user.getEmail());
    }

    /**
     * Отримує користувача за Keycloak ID
     */
    public UserResponse getUserByKeycloakId(String keycloakId) {
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> BusinessException.notFound("Користувача не знайдено"));
        
        return userMapper.toResponse(user);
    }

    /**
     * Капіталізує першу літеру рядка
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}