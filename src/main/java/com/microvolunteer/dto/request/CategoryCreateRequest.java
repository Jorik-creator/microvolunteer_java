package com.microvolunteer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запит на створення категорії")
public class CategoryCreateRequest {
    
    @NotBlank(message = "Назва категорії обов'язкова")
    @Size(max = 100, message = "Назва категорії не може перевищувати 100 символів")
    @Schema(description = "Назва категорії", example = "Освіта", required = true)
    private String name;
    
    @Size(max = 500, message = "Опис не може перевищувати 500 символів")
    @Schema(description = "Опис категорії", example = "Допомога в навчанні та освітні проекти")
    private String description;
}
