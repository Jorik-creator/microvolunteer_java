package com.microvolunteer.service;

import com.microvolunteer.dto.request.UserRegistrationRequest;
import com.microvolunteer.dto.response.UserResponse;
import com.microvolunteer.entity.User;
import com.microvolunteer.exception.BusinessException;
import com.microvolunteer.mapper.UserMapper;
import com.microvolunteer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${keycloak.auth-server-url:http://localhost:8080}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm:microvolunteer}")
    private String realm;

    @Value("${keycloak.admin.username:admin}")
    private String adminUsername;

    @Value("${keycloak.admin.password:admin}")
    private String adminPassword;

    @Transactional
    public UserResponse register(UserRegistrationRequest request) {
        log.info("Реєстрація нового користувача: {}", request.getUsername());

        // Перевірка унікальності username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw BusinessException.conflict("Користувач з таким ім'ям вже існує");
        }

        // Перевірка унікальності email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw BusinessException.conflict("Користувач з таким email вже існує");
        }

        // Створення користувача в Keycloak
        String keycloakId = createKeycloakUser(request);

        // Створення користувача в локальній БД
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setKeycloakId(keycloakId);

        User savedUser = userRepository.save(user);
        log.info("Користувач {} успішно зареєстрований", savedUser.getUsername());

        return userMapper.toResponse(savedUser);
    }

    @Transactional
    public UserResponse syncKeycloakUser(String keycloakId, String username, String email) {
        log.info("Синхронізація користувача з Keycloak ID: {}", keycloakId);

        User user = userRepository.findByKeycloakId(keycloakId).orElse(null);

        if (user == null) {
            // Створення нового користувача якщо він не існує
            user = User.builder()
                    .keycloakId(keycloakId)
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode("temp-password")) // тимчасовий пароль
                    .isActive(true)
                    .build();
        } else {
            // Оновлення існуючих даних
            user.setUsername(username);
            user.setEmail(email);
            user.setLastLogin(LocalDateTime.now());
        }

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    public String generateToken(String keycloakId) {
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> BusinessException.notFound("Користувача не знайдено"));

        return jwtService.generateToken(keycloakId, user.getUsername(), user.getEmail());
    }

    private String createKeycloakUser(UserRegistrationRequest request) {
        try (Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(keycloakServerUrl)
                .realm("master")
                .username(adminUsername)
                .password(adminPassword)
                .clientId("admin-cli")
                .build()) {

            // Створення користувача в Keycloak
            UserRepresentation user = new UserRepresentation();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEnabled(true);
            user.setEmailVerified(true);

            // Встановлення пароля
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(request.getPassword());
            credential.setTemporary(false);
            user.setCredentials(Collections.singletonList(credential));

            // Створення користувача
            try {
                keycloak.realm(realm).users().create(user);
                
                // Отримання ID створеного користувача через пошук
                var users = keycloak.realm(realm).users().searchByUsername(request.getUsername(), true);
                if (!users.isEmpty()) {
                    return users.get(0).getId();
                } else {
                    throw new RuntimeException("Користувача створено, але не вдалося знайти його ID");
                }
            } catch (Exception e) {
                log.error("Помилка при створенні користувача в Keycloak: {}", e.getMessage());
                throw new RuntimeException("Помилка створення користувача в Keycloak: " + e.getMessage());
            }

        } catch (Exception e) {
            log.error("Помилка при створенні користувача в Keycloak", e);
            throw BusinessException.badRequest("Помилка при створенні користувача в системі авторизації");
        }
    }
}