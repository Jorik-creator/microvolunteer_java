package com.microvolunteer.mapper;

import com.microvolunteer.dto.response.CategoryResponse;
import com.microvolunteer.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Category entity and DTOs.
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {
    
    /**
     * Convert Category entity to CategoryResponse DTO
     */
    @Mapping(target = "taskCount", ignore = true)
    CategoryResponse toResponse(Category category);
}
