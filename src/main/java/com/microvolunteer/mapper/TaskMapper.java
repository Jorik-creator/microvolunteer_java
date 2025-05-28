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

    @Mapping(target = "currentVolunteers", expression = "java(task.getCurrentVolunteers())")
    @Mapping(target = "availableSpots", expression = "java(task.getMaxVolunteers() - task.getCurrentVolunteers())")
    @Mapping(target = "pastDue", expression = "java(task.getDeadline() != null && task.getDeadline().isBefore(java.time.LocalDateTime.now()))")
    @Mapping(target = "images", source = "images", qualifiedByName = "imagesToUrls")
    @Mapping(target = "canJoin", ignore = true)
    @Mapping(target = "participant", ignore = true)
    @Mapping(target = "status", expression = "java(task.getStatus().name())")
    TaskResponse toResponse(Task task);

    void updateEntityFromRequest(TaskCreateRequest request, @MappingTarget Task task);

    @Named("imagesToUrls")
    default List<String> imagesToUrls(Set<TaskImage> images) {
        return images.stream()
                .map(TaskImage::getImageUrl)
                .collect(Collectors.toList());
    }
}