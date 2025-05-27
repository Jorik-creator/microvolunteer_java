package com.microvolunteer.dto.request;

import com.microvolunteer.enums.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest {

    @NotBlank(message = "Ім'я користувача є обов'язковим")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,30}$",
            message = "Ім'я користувача має містити лише літери, цифри та підкреслення (3-30 символів)")
    private String username;

    @NotBlank(message = "Email є обов'язковим")
    @Email(message = "Невірний формат email")
    private String email;

    @NotBlank(message = "Пароль є обов'язковим")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
            message = "Пароль має містити мінімум 8 символів, включаючи цифри, великі та малі літери, спеціальні символи")
    private String password;

    @NotNull(message = "Тип користувача є обов'язковим")
    private UserType userType;

    private String firstName;
    private String lastName;
    private String phone;
    private String address;
}