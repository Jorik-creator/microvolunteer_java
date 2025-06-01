package com.microvolunteer.service;

import com.microvolunteer.dto.response.UserResponse;
import com.microvolunteer.entity.User;
import com.microvolunteer.enums.UserType;
import com.microvolunteer.exception.BusinessException;
import com.microvolunteer.mapper.UserMapper;
import com.microvolunteer.repository.ParticipationRepository;
import com.microvolunteer.repository.TaskRepository;
import com.microvolunteer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final TaskRepository taskRepository;
    private final ParticipationRepository participationRepository;

    public List<UserResponse> getAllUsers() {
        log.info("Fetching all users");
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toResponse)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        log.info("Fetching user with ID: {}", id);
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new BusinessException("User not found with id: " + id));
    }

    public Optional<UserResponse> getUserByEmail(String email) {
        log.info("Fetching user with email: {}", email);
        return userRepository.findByEmail(email)
                .map(userMapper::toResponse);
    }

    // Додано метод для отримання користувача за Keycloak ID
    public UserResponse getUserByKeycloakId(String keycloakId) {
        log.info("Fetching user with Keycloak ID: {}", keycloakId);
        return userRepository.findByKeycloakId(keycloakId)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new BusinessException("User not found with keycloak id: " + keycloakId));
    }

    // Додано метод для отримання User entity за Keycloak ID  
    public User getUserEntityByKeycloakId(String keycloakId) {
        log.info("Fetching user entity with Keycloak ID: {}", keycloakId);
        return userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new BusinessException("User not found with keycloak id: " + keycloakId));
    }

    public Optional<User> getUserEntityById(Long id) {
        return userRepository.findById(id);
    }

    public List<UserResponse> getActiveUsers() {
        log.info("Fetching active users");
        List<User> users = userRepository.findByIsActiveTrue();
        return users.stream()
                .map(userMapper::toResponse)
                .toList();
    }

    public List<UserResponse> getUsersByType(UserType userType) {
        log.info("Fetching users with type: {}", userType);
        List<User> users = userRepository.findByUserType(userType);
        return users.stream()
                .map(userMapper::toResponse)
                .toList();
    }

    // Додано метод для отримання статистики користувача
    public Map<String, Object> getUserStatistics(Long userId) {
        log.info("Fetching statistics for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found with id: " + userId));
        
        Map<String, Object> statistics = new HashMap<>();
        
        // Кількість створених завдань
        int createdTasks = taskRepository.findByCreatorId(userId).size();
        statistics.put("createdTasks", createdTasks);
        
        // Кількість участей у завданнях
        int participations = participationRepository.findByUserId(userId).size();
        statistics.put("participations", participations);
        
        // Загальна активність
        statistics.put("totalActivity", createdTasks + participations);
        
        // Дата реєстрації
        statistics.put("memberSince", user.getCreatedAt());
        
        // Останній вхід
        statistics.put("lastLogin", user.getLastLoginAt());
        
        // Тип користувача
        statistics.put("userType", user.getUserType());
        
        return statistics;
    }

    // Додано метод для оновлення профілю
    @Transactional
    public UserResponse updateProfile(String keycloakId, Map<String, String> updates) {
        log.info("Updating profile for user with Keycloak ID: {}", keycloakId);
        
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new BusinessException("User not found with keycloak id: " + keycloakId));
        
        // Оновлення полів з валідацією
        if (updates.containsKey("firstName")) {
            String firstName = updates.get("firstName");
            if (firstName == null || firstName.trim().isEmpty()) {
                throw new BusinessException("First name cannot be empty");
            }
            user.setFirstName(firstName.trim());
        }
        
        if (updates.containsKey("lastName")) {
            String lastName = updates.get("lastName");
            if (lastName == null || lastName.trim().isEmpty()) {
                throw new BusinessException("Last name cannot be empty");
            }
            user.setLastName(lastName.trim());
        }
        
        if (updates.containsKey("phone")) {
            String phone = updates.get("phone");
            if (phone != null && !phone.trim().isEmpty()) {
                user.setPhone(phone.trim());
            }
        }
        
        if (updates.containsKey("email")) {
            String email = updates.get("email");
            if (email != null && !email.equals(user.getEmail())) {
                if (!isValidEmail(email)) {
                    throw new BusinessException("Invalid email format");
                }
                if (userRepository.findByEmail(email).isPresent()) {
                    throw new BusinessException("Email already exists");
                }
                user.setEmail(email);
            }
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        
        return userMapper.toResponse(savedUser);
    }

    @Transactional
    public UserResponse createUser(User user) {
        log.info("Creating new user with email: {}", user.getEmail());
        
        // Валідації
        validateUserCreation(user);
        
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Transactional
    public Optional<UserResponse> updateUser(Long id, User updatedUser) {
        log.info("Updating user with ID: {}", id);
        
        return userRepository.findById(id)
                .map(user -> {
                    validateUserUpdate(user, updatedUser);
                    
                    if (updatedUser.getFirstName() != null) {
                        user.setFirstName(updatedUser.getFirstName());
                    }
                    if (updatedUser.getLastName() != null) {
                        user.setLastName(updatedUser.getLastName());
                    }
                    if (updatedUser.getPhone() != null) {
                        user.setPhone(updatedUser.getPhone());
                    }
                    if (updatedUser.getUserType() != null) {
                        user.setUserType(updatedUser.getUserType());
                    }
                    
                    user.setUpdatedAt(LocalDateTime.now());
                    User savedUser = userRepository.save(user);
                    return userMapper.toResponse(savedUser);
                });
    }

    @Transactional
    public boolean deactivateUser(Long id) {
        log.info("Deactivating user with ID: {}", id);
        
        return userRepository.findById(id)
                .map(user -> {
                    validateUserDeactivation(user);
                    
                    user.setIsActive(false);
                    user.setUpdatedAt(LocalDateTime.now());
                    userRepository.save(user);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public void updateLastLogin(Long userId) {
        userRepository.findById(userId)
                .ifPresent(user -> {
                    user.setLastLoginAt(LocalDateTime.now());
                    userRepository.save(user);
                });
    }

    // Валідації
    private void validateUserCreation(User user) {
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new BusinessException("Email cannot be empty");
        }
        
        if (!isValidEmail(user.getEmail())) {
            throw new BusinessException("Invalid email format");
        }
        
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new BusinessException("User with this email already exists");
        }
        
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            throw new BusinessException("First name cannot be empty");
        }
        
        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            throw new BusinessException("Last name cannot be empty");
        }
        
        if (user.getUserType() == null) {
            user.setUserType(UserType.VOLUNTEER); // За замовчуванням
        }
    }

    private void validateUserUpdate(User existingUser, User updatedUser) {
        if (!existingUser.getIsActive()) {
            throw new BusinessException("Cannot update inactive user");
        }
        
        // Перевірити чи змінюється email
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.findByEmail(updatedUser.getEmail()).isPresent()) {
                throw new BusinessException("User with this email already exists");
            }
            if (!isValidEmail(updatedUser.getEmail())) {
                throw new BusinessException("Invalid email format");
            }
        }
    }

    private void validateUserDeactivation(User user) {
        if (!user.getIsActive()) {
            throw new BusinessException("User is already inactive");
        }
        
        // Тут можна додати перевірку чи користувач не має активних завдань/участі
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}
