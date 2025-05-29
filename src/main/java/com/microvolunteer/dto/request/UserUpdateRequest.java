package com.microvolunteer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Запит на оновлення даних користувача")
public class UserUpdateRequest {
    
    @Size(max = 150)
    @Schema(description = "Ім'я", example = "John")
    private String firstName;
    
    @Size(max = 150)
    @Schema(description = "Прізвище", example = "Doe")
    private String lastName;
    
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Невірний формат телефону")
    @Schema(description = "Номер телефону", example = "+380501234567")
    private String phone;
    
    @Size(max = 1000)
    @Schema(description = "Короткий опис про себе", example = "Люблю допомагати людям")
    private String bio;
    
    @Size(max = 255)
    @Schema(description = "Адреса", example = "Київ, Україна")
    private String address;
}
