package com.microvolunteer.mapper;

import com.microvolunteer.dto.response.CategoryResponse;
import com.microvolunteer.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);
}