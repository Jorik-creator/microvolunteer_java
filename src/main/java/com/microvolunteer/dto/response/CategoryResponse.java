package com.microvolunteer.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO for category response.
 */
@Schema(description = "Category response")
public class CategoryResponse {
    
    @Schema(description = "Category ID", example = "1")
    private Long id;
    
    @Schema(description = "Category name", example = "Shopping")
    private String name;
    
    @Schema(description = "Category description", example = "Tasks related to shopping and errands")
    private String description;
    
    @Schema(description = "Whether category is active", example = "true")
    private Boolean active;
    
    @Schema(description = "Category creation timestamp", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Category last update timestamp", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;
    
    @Schema(description = "Number of tasks in this category", example = "15")
    private Integer taskCount;
    
    // Constructors
    public CategoryResponse() {}
    
    public CategoryResponse(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
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
    
    public Integer getTaskCount() {
        return taskCount;
    }
    
    public void setTaskCount(Integer taskCount) {
        this.taskCount = taskCount;
    }
}
