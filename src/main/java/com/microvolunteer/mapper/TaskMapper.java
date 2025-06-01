package com.microvolunteer.mapper;

import com.microvolunteer.dto.request.TaskCreateRequest;
import com.microvolunteer.dto.response.TaskResponse;
import com.microvolunteer.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {UserMapper.class, CategoryMapper.class})
public interface TaskMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", constant = "OPEN")
    @Mapping(target = "participations", ignore = true)
    Task toEntity(TaskCreateRequest request);

    @Mapping(target = "creatorName", expression = "java(task.getCreator().getFirstName() + \" \" + task.getCreator().getLastName())")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "participantCount", expression = "java(task.getParticipations() != null ? task.getParticipations().size() : 0)")
    @Mapping(target = "creator", source = "creator")
    @Mapping(target = "category", source = "category")
    TaskResponse toResponse(Task task);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "participations", ignore = true)
    void updateEntityFromRequest(TaskCreateRequest request, @MappingTarget Task task);
}