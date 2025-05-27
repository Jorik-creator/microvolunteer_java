package com.microvolunteer.dto.response;

import com.microvolunteer.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private UserType userType;
    private String phone;
    private String bio;
    private String address;
    private String profileImage;
    private LocalDateTime dateJoined;
    private boolean isActive;
}