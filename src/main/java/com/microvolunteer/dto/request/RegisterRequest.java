package com.microvolunteer.dto.request;

import com.microvolunteer.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запит на реєстрацію нового користувача")
public class RegisterRequest {
    
    @NotBlank(message = "Ім'я користувача обов'язкове")
    @Size(min = 3, max = 50, message = "Ім'я користувача має бути від 3 до 50 символів")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Ім'я користувача може містити лише літери, цифри та підкреслення")
    @Schema(description = "Ім'я користувача", example = "john_doe", required = true)
    private String username;
    
    @NotBlank(message = "Email обов'язковий")
    @Email(message = "Невірний формат email")
    @Schema(description = "Email адреса", example = "john.doe@example.com", required = true)
    private String email;
    
    @NotBlank(message = "Пароль обов'язковий")
    @Size(min = 8, message = "Пароль має містити мінімум 8 символів")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z]).*$", 
             message = "Пароль має містити хоча б одну літеру та одну цифру")
    @Schema(description = "Пароль", example = "password123", required = true)
    private String password;
    
    @NotBlank(message = "Ім'я обов'язкове")
    @Size(max = 150, message = "Ім'я не може перевищувати 150 символів")
    @Schema(description = "Ім'я користувача", example = "John", required = true)
    private String firstName;
    
    @NotBlank(message = "Прізвище обов'язкове")
    @Size(max = 150, message = "Прізвище не може перевищувати 150 символів")
    @Schema(description = "Прізвище користувача", example = "Doe", required = true)
    private String lastName;
    
    @NotNull(message = "Тип користувача обов'язковий")
    @Schema(description = "Тип користувача", example = "VOLUNTEER", required = true)
    private UserType userType;
    
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Невірний формат телефону")
    @Schema(description = "Номер телефону", example = "+380501234567")
    private String phone;
}
