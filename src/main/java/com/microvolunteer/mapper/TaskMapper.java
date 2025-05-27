package com.microvolunteer.mapper;

import com.microvolunteer.dto.request.TaskCreateRequest;
import com.microvolunteer.dto.response.TaskResponse;
import com.microvolunteer.entity.Task;
import com.microvolunteer.entity.TaskImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {UserMapper.class, CategoryMapper.class})
public interface TaskMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", constant = "OPEN")
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "participants", ignore = true)
    Task toEntity(TaskCreateRequest request);

    @Mapping(target = "currentParticipants", expression = "java(task.getParticipants().size())")
    @Mapping(target = "availableSpots", expression = "java(task.getAvailableSpots())")
    @Mapping(target = "isPastDue", expression = "java(task.isPastDue())")
    @Mapping(target = "images", source = "images", qualifiedByName = "imagesToUrls")
    @Mapping(target = "canJoin", ignore = true)
    @Mapping(target = "isParticipant", ignore = true)
    TaskResponse toResponse(Task task);

    void updateEntityFromRequest(TaskCreateRequest request, @MappingTarget Task task);

    @Named("imagesToUrls")
    default List<String> imagesToUrls(Set<TaskImage> images) {
        return images.stream()
                .map(TaskImage::getImageUrl)
                .collect(Collectors.toList());
    }
}