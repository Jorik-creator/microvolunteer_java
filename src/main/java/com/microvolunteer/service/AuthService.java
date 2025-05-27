package com.microvolunteer.service;

import com.microvolunteer.dto.request.UserRegistrationRequest;
import com.microvolunteer.dto.response.UserResponse;
import com.microvolunteer.entity.User;
import com.microvolunteer.exception.BusinessException;
import com.microvolunteer.mapper.UserMapper;
import com.microvolunteer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

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

        // Створення нового користувача
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Збереження користувача
        User savedUser = userRepository.save(user);
        log.info("Користувач {} успішно зареєстрований", savedUser.getUsername());

        return userMapper.toResponse(savedUser);
    }

    @Transactional
    public void linkKeycloakId(String username, String keycloakId) {
        log.info("Прив'язка Keycloak ID {} до користувача {}", keycloakId, username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> BusinessException.notFound("Користувача не знайдено"));

        user.setKeycloakId(keycloakId);
        userRepository.save(user);
    }
}