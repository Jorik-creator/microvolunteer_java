package com.microvolunteer.mapper;

import com.microvolunteer.dto.request.UserRegistrationRequest;
import com.microvolunteer.dto.response.UserResponse;
import com.microvolunteer.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "bio", ignore = true)
    @Mapping(target = "profileImage", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "dateJoined", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "keycloakId", ignore = true)
    @Mapping(target = "createdTasks", ignore = true)
    @Mapping(target = "participations", ignore = true)
    User toEntity(UserRegistrationRequest request);

    UserResponse toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "bio", ignore = true)
    @Mapping(target = "profileImage", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "dateJoined", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "keycloakId", ignore = true)
    @Mapping(target = "createdTasks", ignore = true)
    @Mapping(target = "participations", ignore = true)
    void updateEntityFromRequest(UserRegistrationRequest request, @MappingTarget User user);
}