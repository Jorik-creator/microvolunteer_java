package com.microvolunteer.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Відповідь на авторизацію")
public class AuthResponse {
    
    @Schema(description = "JWT токен доступу", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;
    
    @Schema(description = "Refresh токен", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;
    
    @Schema(description = "Тип токену", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";
    
    @Schema(description = "Час життя токену в секундах", example = "3600")
    private Long expiresIn;
    
    @Schema(description = "ID користувача", example = "1")
    private Long userId;
    
    @Schema(description = "Ім'я користувача", example = "john_doe")
    private String username;
    
    @Schema(description = "Email користувача", example = "john.doe@example.com")
    private String email;
    
    @Schema(description = "Тип користувача", example = "VOLUNTEER")
    private String userType;
    
    @Schema(description = "Ім'я", example = "John")
    private String firstName;
    
    @Schema(description = "Прізвище", example = "Doe")
    private String lastName;
}
