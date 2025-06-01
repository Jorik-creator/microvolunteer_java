package com.microvolunteer.service;

import com.microvolunteer.config.KeycloakUtils;
import com.microvolunteer.dto.request.UserRegistrationRequest;
import com.microvolunteer.dto.response.UserResponse;
import com.microvolunteer.entity.User;
import com.microvolunteer.enums.UserType;
import com.microvolunteer.exception.BusinessException;
import com.microvolunteer.mapper.UserMapper;
import com.microvolunteer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final KeycloakUtils keycloakUtils;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse registerUser(UserRegistrationRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new BusinessException("User with email " + request.getEmail() + " already exists");
        }

        String keycloakUserId = keycloakUtils.createUser(
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName()
        );

        User user = User.builder()
                .keycloakId(keycloakUserId)
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .userType(UserType.VOLUNTEER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        return userMapper.toResponse(savedUser);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByKeycloakId(String keycloakId) {
        return userRepository.findByKeycloakId(keycloakId);
    }

    @Transactional
    public void updateLastLogin(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastLoginAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    @Transactional
    public void deactivateUser(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setIsActive(false);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        });
    }
}
