package com.microvolunteer.mapper;

import com.microvolunteer.dto.request.UserRegistrationRequest;
import com.microvolunteer.dto.response.UserResponse;
import com.microvolunteer.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for User entity and DTOs.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {
    
    /**
     * Convert User entity to UserResponse DTO
     */
    UserResponse toResponse(User user);
    
    /**
     * Convert UserRegistrationRequest DTO to User entity
     * Note: keycloakSubject will be set separately in the service
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "keycloakSubject", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "authoredTasks", ignore = true)
    @Mapping(target = "participations", ignore = true)
    User toEntity(UserRegistrationRequest request);
    
    /**
     * Update existing User entity from UserRegistrationRequest
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "keycloakSubject", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "authoredTasks", ignore = true)
    @Mapping(target = "participations", ignore = true)
    void updateEntity(UserRegistrationRequest request, @MappingTarget User user);
}
