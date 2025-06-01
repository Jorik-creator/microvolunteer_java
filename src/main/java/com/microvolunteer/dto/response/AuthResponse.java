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
@Schema(description = "Відповідь на синхронізацію з Keycloak")
public class AuthResponse {
    
    @Schema(description = "Повідомлення про успіх", example = "Користувач успішно синхронізований")
    private String message;
    
    @Schema(description = "ID користувача", example = "1")
    private Long userId;
    
    @Schema(description = "Ім'я користувача", example = "john_doe")
    private String username;
    
    @Schema(description = "Email користувача", example = "john.doe@example.com")
    private String email;
    
    @Schema(description = "Тип користувача", example = "VOLUNTEER")
    private String userType;
}
