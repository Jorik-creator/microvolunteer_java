package com.microvolunteer.dto.request;

import com.microvolunteer.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for user registration request.
 */
@Schema(description = "User registration request")
public class UserRegistrationRequest {
    
    @Schema(description = "User's first name", example = "John", required = true)
    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    private String firstName;
    
    @Schema(description = "User's last name", example = "Doe", required = true)
    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    private String lastName;
    
    @Schema(description = "User's email address", example = "john.doe@example.com", required = true)
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;
    
    @Schema(description = "User's phone number", example = "+1234567890")
    @Size(max = 15, message = "Phone number cannot exceed 15 characters")
    private String phone;
    
    @Schema(description = "User description/bio", example = "I'm a volunteer looking to help others")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    @Schema(description = "Type of user", example = "VOLUNTEER", required = true)
    @NotNull(message = "User type is required")
    private UserType userType;
    
    // Constructors
    public UserRegistrationRequest() {}
    
    public UserRegistrationRequest(String firstName, String lastName, String email, UserType userType) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.userType = userType;
    }
    
    // Getters and Setters
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
}
