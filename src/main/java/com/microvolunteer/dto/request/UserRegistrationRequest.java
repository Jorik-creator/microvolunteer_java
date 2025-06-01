package com.microvolunteer.dto.request;

import com.microvolunteer.enums.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotBlank(message = "First name is required")  // Виправлено повідомлення
    @Size(max = 100, message = "First name must not exceed 100 characters")  // Виправлено повідомлення
    private String firstName;  // Виправлено з firstN

    @NotBlank(message = "Last name is required")  // Виправлено повідомлення
    @Size(max = 100, message = "Last name must not exceed 100 characters")  // Виправлено повідомлення
    private String lastName;   // Виправлено з lastN

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phone;
    
    @NotNull(message = "User type is required")
    private UserType userType;
}
