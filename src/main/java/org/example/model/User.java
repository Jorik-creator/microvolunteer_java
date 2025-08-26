package org.example.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @Column(unique = true, nullable = false)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @Column(nullable = false)
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    @Column(name = "first_name", nullable = false, length = 50)
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;
    
    @Column(name = "last_name", nullable = false, length = 50)
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    @NotNull(message = "User type is required")
    private UserType userType;
    
    @Column(length = 20)
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phone;
    
    @Column(columnDefinition = "TEXT")
    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    private String bio;
    
    @Column(columnDefinition = "TEXT")
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;
    
    @Column(name = "profile_image_url")
    private String profileImageUrl;
    
    @CreationTimestamp
    @Column(name = "date_joined", nullable = false, updatable = false)
    private LocalDateTime dateJoined;
    
    @UpdateTimestamp
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Task> createdTasks = new HashSet<>();
    
    @ManyToMany(mappedBy = "participants", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Task> participatingTasks = new HashSet<>();
}