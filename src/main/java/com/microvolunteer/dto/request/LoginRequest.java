package com.microvolunteer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запит на вхід в систему")
public class LoginRequest {
    
    @NotBlank(message = "Ім'я користувача обов'язкове")
    @Schema(description = "Ім'я користувача", example = "john_doe", required = true)
    private String username;
    
    @NotBlank(message = "Пароль обов'язковий")
    @Schema(description = "Пароль користувача", example = "password123", required = true)
    private String password;
}
