package com.microvolunteer.dto.response;

import com.microvolunteer.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO for user response.
 */
@Schema(description = "User response")
public class UserResponse {
    
    @Schema(description = "User ID", example = "1")
    private Long id;
    
    @Schema(description = "User's first name", example = "John")
    private String firstName;
    
    @Schema(description = "User's last name", example = "Doe")
    private String lastName;
    
    @Schema(description = "User's email address", example = "john.doe@example.com")
    private String email;
    
    @Schema(description = "User's phone number", example = "+1234567890")
    private String phone;
    
    @Schema(description = "User description/bio", example = "I'm a volunteer looking to help others")
    private String description;
    
    @Schema(description = "Type of user", example = "VOLUNTEER")
    private UserType userType;
    
    @Schema(description = "Whether user is active", example = "true")
    private Boolean active;
    
    @Schema(description = "User creation timestamp", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "User last update timestamp", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;
    
    // Constructors
    public UserResponse() {}
    
    public UserResponse(Long id, String firstName, String lastName, String email, UserType userType) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.userType = userType;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public UserType getUserType() {
        return userType;
    }
    
    public void setUserType(UserType userType) {
        this.userType = userType;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
