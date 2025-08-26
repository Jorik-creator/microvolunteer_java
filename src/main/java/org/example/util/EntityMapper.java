package org.example.util;

import org.example.dto.*;
import org.example.model.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EntityMapper {

    public UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userType(user.getUserType() != null ? user.getUserType().toString() : null)
                .phone(user.getPhone())
                .bio(user.getBio())
                .address(user.getAddress())
                .profileImageUrl(user.getProfileImageUrl())
                .dateJoined(user.getDateJoined())
                .lastUpdated(user.getLastUpdated())
                .isActive(user.getIsActive())
                .build();
    }

    public CategoryResponse toCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .build();
    }

    public CategoryResponse toCategoryResponse(Category category, Long tasksCount) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .tasksCount(tasksCount)
                .build();
    }

    public TaskResponse toTaskResponse(Task task) {
        return toTaskResponse(task, true);
    }
    
    public TaskResponse toTaskResponse(Task task, boolean includeCollections) {
        TaskResponse.TaskResponseBuilder builder = TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .location(task.getLocation())
                .startDate(task.getStartDate())
                .endDate(task.getEndDate())
                .maxParticipants(task.getMaxParticipants())
                .status(task.getStatus().name())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt());
                
        if (task.getCreator() != null) {
            builder.creator(toUserResponse(task.getCreator()));
        }
        if (task.getCategory() != null) {
            builder.category(toCategoryResponse(task.getCategory()));
        }
        
        if (includeCollections) {
            try {
                builder.currentParticipants(task.getParticipants() != null ? task.getParticipants().size() : 0);
                builder.participants(task.getParticipants() != null ? 
                    task.getParticipants().stream()
                        .map(this::toUserResponse)
                        .collect(Collectors.toList()) : List.of());
                builder.imageUrls(task.getImages() != null ? 
                    task.getImages().stream()
                        .map(TaskImage::getImageUrl)
                        .collect(Collectors.toList()) : List.of());
            } catch (Exception e) {
                builder.currentParticipants(0);
                builder.participants(List.of());
                builder.imageUrls(List.of());
            }
        } else {
            builder.currentParticipants(0);
            builder.participants(List.of());
            builder.imageUrls(List.of());
        }
        
        return builder.build();
    }

    public User toUser(UserRegistrationRequest request) {
        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .userType(request.getUserType())
                .phone(request.getPhone())
                .bio(request.getBio())
                .address(request.getAddress())
                .isActive(true)
                .build();
    }

    public Category toCategory(CategoryRequest request) {
        return Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
    }

    public Task toTask(TaskRequest request) {
        return Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .maxParticipants(request.getMaxParticipants())
                .status(TaskStatus.OPEN)
                .build();
    }

    public void updateUserFromRequest(User user, UserUpdateRequest request) {
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getProfileImageUrl() != null) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }
    }

    public void updateTaskFromRequest(Task task, TaskUpdateRequest request) {
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getLocation() != null) {
            task.setLocation(request.getLocation());
        }
        if (request.getStartDate() != null) {
            task.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            task.setEndDate(request.getEndDate());
        }
        if (request.getMaxParticipants() != null) {
            task.setMaxParticipants(request.getMaxParticipants());
        }
    }

    public void updateCategoryFromRequest(Category category, CategoryRequest request) {
        if (request.getName() != null) {
            category.setName(request.getName());
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
    }
}