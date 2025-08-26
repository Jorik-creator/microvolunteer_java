package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.UserResponse;
import org.example.dto.UserStatisticsResponse;
import org.example.dto.UserUpdateRequest;
import org.example.exception.BadRequestException;
import org.example.exception.ResourceNotFoundException;
import org.example.model.User;
import org.example.model.UserType;
import org.example.repository.UserRepository;
import org.example.specification.UserSpecification;
import org.example.util.EntityMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EntityMapper entityMapper;
    private final PasswordEncoder passwordEncoder;

    public Page<UserResponse> getAllUsers(String username, String email, 
                                         UserType userType, Boolean isActive, 
                                         Pageable pageable) {
        Page<User> users;
        
        // Apply filters if provided
        if (userType != null) {
            users = userRepository.findByUserType(userType, pageable);
        } else if (isActive != null) {
            users = userRepository.findByIsActive(isActive, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        
        // Apply additional filters in memory if needed
        if (username != null || email != null) {
            List<User> filteredUsers = users.getContent().stream()
                .filter(user -> username == null || user.getUsername().toLowerCase().contains(username.toLowerCase()))
                .filter(user -> email == null || user.getEmail().toLowerCase().contains(email.toLowerCase()))
                .collect(java.util.stream.Collectors.toList());
            
            List<UserResponse> userResponses = filteredUsers.stream()
                .map(entityMapper::toUserResponse)
                .collect(java.util.stream.Collectors.toList());
                
            return new org.springframework.data.domain.PageImpl<>(
                userResponses, pageable, filteredUsers.size()
            );
        }
        
        return users.map(entityMapper::toUserResponse);
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return entityMapper.toUserResponse(user);
    }

    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return entityMapper.toUserResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email is already in use!");
            }
        }

        entityMapper.updateUserFromRequest(user, request);
        User updatedUser = userRepository.save(user);
        return entityMapper.toUserResponse(updatedUser);
    }

    @Transactional
    public UserResponse updateProfile(String username, UserUpdateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email is already in use!");
            }
        }

        entityMapper.updateUserFromRequest(user, request);
        User updatedUser = userRepository.save(user);
        return entityMapper.toUserResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setIsActive(false);
        userRepository.save(user);
    }

    public UserStatisticsResponse getUserStatistics(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        return getUserStatisticsById(user.getId());
    }

    public UserStatisticsResponse getUserStatisticsById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Re-enabled statistics with proper exception handling
        Long completedTasks;
        Long cancelledTasks;
        Long participatedTasks;
        Long volunteersHelped;
        
        try {
            completedTasks = userRepository.countTasksByUserIdAndStatus(userId, 
                org.example.model.TaskStatus.COMPLETED);
            cancelledTasks = userRepository.countTasksByUserIdAndStatus(userId, 
                org.example.model.TaskStatus.CANCELLED);
            participatedTasks = userRepository.countCompletedParticipationsByUserId(userId, 
                org.example.model.TaskStatus.COMPLETED);
            volunteersHelped = userRepository.countVolunteersHelpedByUserId(userId);
        } catch (Exception e) {
            // Fallback to manual calculation if database queries fail
            completedTasks = (long) user.getCreatedTasks().stream()
                .mapToInt(task -> task.getStatus() == org.example.model.TaskStatus.COMPLETED ? 1 : 0).sum();
            cancelledTasks = (long) user.getCreatedTasks().stream()
                .mapToInt(task -> task.getStatus() == org.example.model.TaskStatus.CANCELLED ? 1 : 0).sum();
            participatedTasks = (long) user.getParticipatingTasks().size();
            volunteersHelped = 0L;
        }

        return UserStatisticsResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .userType(user.getUserType().name())
                .dateJoined(user.getDateJoined())
                .totalCreatedTasks((long) user.getCreatedTasks().size())
                .totalCompletedTasks(completedTasks)
                .totalCancelledTasks(cancelledTasks)
                .totalParticipatedTasks(participatedTasks)
                .totalVolunteersHelped(volunteersHelped != null ? volunteersHelped : 0L)
                .build();
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

}