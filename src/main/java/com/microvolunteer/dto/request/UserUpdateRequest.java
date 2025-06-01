package com.microvolunteer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запит на оновлення даних користувача")
public class UserUpdateRequest {
    
    @Size(max = 150)
    @Schema(description = "Ім'я", example = "John")
    private String firstName;
    
    @Size(max = 150)
    @Schema(description = "Прізвище", example = "Doe")
    private String lastName;
}
