package com.microvolunteer.mapper;

import com.microvolunteer.dto.request.TaskCreateRequest;
import com.microvolunteer.dto.response.TaskResponse;
import com.microvolunteer.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Task entity and DTOs.
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class, CategoryMapper.class})
public interface TaskMapper {
    
    /**
     * Convert Task entity to TaskResponse DTO
     */
    @Mapping(target = "participantsCount", ignore = true)
    TaskResponse toResponse(Task task);
    
    /**
     * Convert TaskCreateRequest DTO to Task entity
     * Note: author, categories, and other fields will be set separately in the service
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "OPEN")
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "participations", ignore = true)
    Task toEntity(TaskCreateRequest request);
}
