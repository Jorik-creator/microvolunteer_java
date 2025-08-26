package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String userType;
    private String phone;
    private String bio;
    private String address;
    private String profileImageUrl;
    private LocalDateTime dateJoined;
    private LocalDateTime lastUpdated;
    private Boolean isActive;
}