package com.microvolunteer.mapper;

import com.microvolunteer.dto.request.CategoryCreateRequest;
import com.microvolunteer.dto.response.CategoryResponse;
import com.microvolunteer.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    Category toEntity(CategoryCreateRequest request);
}
