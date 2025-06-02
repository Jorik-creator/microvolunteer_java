package com.microvolunteer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for task creation request.
 */
@Schema(description = "Task creation request")
public class TaskCreateRequest {
    
    @Schema(description = "Task title", example = "Help with grocery shopping", required = true)
    @NotBlank(message = "Task title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    private String title;
    
    @Schema(description = "Detailed task description", 
            example = "I need help with weekly grocery shopping. I have mobility issues and cannot carry heavy bags.", 
            required = true)
    @NotBlank(message = "Task description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;
    
    @Schema(description = "Task location", example = "Downtown grocery store, 123 Main St")
    private String location;
    
    @Schema(description = "Task deadline", example = "2024-07-15T10:00:00")
    private LocalDateTime deadline;
    
    @Schema(description = "Set of category IDs for this task", example = "[1, 3]")
    private Set<Long> categoryIds;
    
    // Constructors
    public TaskCreateRequest() {}
    
    public TaskCreateRequest(String title, String description) {
        this.title = title;
        this.description = description;
    }
    
    // Getters and Setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public LocalDateTime getDeadline() {
        return deadline;
    }
    
    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }
    
    public Set<Long> getCategoryIds() {
        return categoryIds;
    }
    
    public void setCategoryIds(Set<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }
}
