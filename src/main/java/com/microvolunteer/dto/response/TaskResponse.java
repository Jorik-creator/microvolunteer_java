package com.microvolunteer.dto.response;

import com.microvolunteer.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for task response.
 */
@Schema(description = "Task response")
public class TaskResponse {
    
    @Schema(description = "Task ID", example = "1")
    private Long id;
    
    @Schema(description = "Task title", example = "Help with grocery shopping")
    private String title;
    
    @Schema(description = "Task description", example = "I need help with weekly grocery shopping")
    private String description;
    
    @Schema(description = "Task location", example = "Downtown grocery store, 123 Main St")
    private String location;
    
    @Schema(description = "Task deadline", example = "2024-07-15T10:00:00")
    private LocalDateTime deadline;
    
    @Schema(description = "Task status", example = "OPEN")
    private TaskStatus status;
    
    @Schema(description = "Task author information")
    private UserResponse author;
    
    @Schema(description = "Task creation timestamp", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Task last update timestamp", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;
    
    @Schema(description = "Task completion timestamp", example = "2024-01-20T15:45:00")
    private LocalDateTime completedAt;
    
    @Schema(description = "List of categories associated with this task")
    private List<CategoryResponse> categories;
    
    @Schema(description = "Number of active volunteers participating", example = "2")
    private Integer participantsCount;
    
    // Constructors
    public TaskResponse() {}
    
    public TaskResponse(Long id, String title, String description, TaskStatus status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public TaskStatus getStatus() {
        return status;
    }
    
    public void setStatus(TaskStatus status) {
        this.status = status;
    }
    
    public UserResponse getAuthor() {
        return author;
    }
    
    public void setAuthor(UserResponse author) {
        this.author = author;
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
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public List<CategoryResponse> getCategories() {
        return categories;
    }
    
    public void setCategories(List<CategoryResponse> categories) {
        this.categories = categories;
    }
    
    public Integer getParticipantsCount() {
        return participantsCount;
    }
    
    public void setParticipantsCount(Integer participantsCount) {
        this.participantsCount = participantsCount;
    }
}
