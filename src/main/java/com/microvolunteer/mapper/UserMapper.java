package com.microvolunteer.mapper;

import com.microvolunteer.dto.response.UserResponse;
import com.microvolunteer.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserResponse toResponse(User user);
}
