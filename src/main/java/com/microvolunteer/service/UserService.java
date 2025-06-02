package com.microvolunteer.service;

import com.microvolunteer.dto.request.UserRegistrationRequest;
import com.microvolunteer.dto.response.UserResponse;
import com.microvolunteer.entity.User;
import com.microvolunteer.enums.UserType;
import com.microvolunteer.exception.BusinessException;
import com.microvolunteer.mapper.UserMapper;
import com.microvolunteer.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for user operations.
 */
@Service
@Transactional
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    
    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }
    
    /**
     * Register a new user
     */
    public UserResponse registerUser(UserRegistrationRequest request, String keycloakSubject) {
        logger.info("Registering new user with email: {}", request.getEmail());
        
        // Check if user already exists by email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw BusinessException.userAlreadyExists(request.getEmail());
        }
        
        // Check if user already exists by Keycloak subject
        if (userRepository.existsByKeycloakSubject(keycloakSubject)) {
            throw BusinessException.userAlreadyExists(keycloakSubject);
        }
        
        User user = userMapper.toEntity(request);
        user.setKeycloakSubject(keycloakSubject);
        
        User savedUser = userRepository.save(user);
        logger.info("Successfully registered user with id: {}", savedUser.getId());
        
        return userMapper.toResponse(savedUser);
    }
    
    /**
     * Get user by Keycloak subject
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByKeycloakSubject(String keycloakSubject) {
        User user = userRepository.findByKeycloakSubject(keycloakSubject)
            .orElseThrow(() -> BusinessException.userNotFound(keycloakSubject));
        
        return userMapper.toResponse(user);
    }
    
    /**
     * Get user entity by Keycloak subject (for internal use)
     */
    @Transactional(readOnly = true)
    public User getUserEntityByKeycloakSubject(String keycloakSubject) {
        return userRepository.findByKeycloakSubject(keycloakSubject)
            .orElseThrow(() -> BusinessException.userNotFound(keycloakSubject));
    }
    
    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> BusinessException.userNotFound(id.toString()));
        
        return userMapper.toResponse(user);
    }
    
    /**
     * Get user entity by ID (for internal use)
     */
    @Transactional(readOnly = true)
    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> BusinessException.userNotFound(id.toString()));
    }
    
    /**
     * Update user profile
     */
    public UserResponse updateUser(String keycloakSubject, UserRegistrationRequest request) {
        logger.info("Updating user profile for subject: {}", keycloakSubject);
        
        User user = getUserEntityByKeycloakSubject(keycloakSubject);
        
        // Check if email is being changed and if it already exists
        if (!user.getEmail().equals(request.getEmail()) && 
            userRepository.existsByEmail(request.getEmail())) {
            throw BusinessException.userAlreadyExists(request.getEmail());
        }
        
        userMapper.updateEntity(request, user);
        User savedUser = userRepository.save(user);
        
        logger.info("Successfully updated user with id: {}", savedUser.getId());
        return userMapper.toResponse(savedUser);
    }
    
    /**
     * Get users by type
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByType(UserType userType) {
        List<User> users = userRepository.findActiveUsersByType(userType);
        return users.stream()
            .map(userMapper::toResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get all volunteers
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getVolunteers() {
        return getUsersByType(UserType.VOLUNTEER);
    }
    
    /**
     * Deactivate user account
     */
    public void deactivateUser(String keycloakSubject) {
        logger.info("Deactivating user with subject: {}", keycloakSubject);
        
        User user = getUserEntityByKeycloakSubject(keycloakSubject);
        user.setActive(false);
        userRepository.save(user);
        
        logger.info("Successfully deactivated user with id: {}", user.getId());
    }
    
    /**
     * Check if user exists by Keycloak subject
     */
    @Transactional(readOnly = true)
    public boolean existsByKeycloakSubject(String keycloakSubject) {
        return userRepository.existsByKeycloakSubject(keycloakSubject);
    }
    
    /**
     * Validate user access to resource
     */
    @Transactional(readOnly = true)
    public void validateUserAccess(String keycloakSubject, Long userId, String operation) {
        User user = getUserEntityByKeycloakSubject(keycloakSubject);
        
        if (!user.getId().equals(userId) && user.getUserType() != UserType.ADMIN) {
            throw BusinessException.unauthorizedAccess(operation);
        }
    }
}
