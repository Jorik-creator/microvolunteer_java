package com.microvolunteer.dto.request;

import com.microvolunteer.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;

/**
 * DTO for task search request with filtering and pagination.
 */
@Schema(description = "Task search request with filters and pagination")
public class TaskSearchRequest {
    
    @Schema(description = "Search text for title and description", example = "grocery shopping")
    private String searchText;
    
    @Schema(description = "Filter by task status", example = "OPEN")
    private TaskStatus status;
    
    @Schema(description = "Filter by category ID", example = "1")
    private Long categoryId;
    
    @Schema(description = "Filter by author ID", example = "5")
    private Long authorId;
    
    @Schema(description = "Filter tasks created from this date", example = "2024-01-01T00:00:00")
    private LocalDateTime fromDate;
    
    @Schema(description = "Filter tasks created until this date", example = "2024-12-31T23:59:59")
    private LocalDateTime toDate;
    
    @Schema(description = "Page number (0-based)", example = "0", defaultValue = "0")
    @Min(value = 0, message = "Page number must be non-negative")
    private int page = 0;
    
    @Schema(description = "Page size", example = "20", defaultValue = "20")
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    private int size = 20;
    
    @Schema(description = "Sort field", example = "createdAt", defaultValue = "createdAt")
    private String sortBy = "createdAt";
    
    @Schema(description = "Sort direction", example = "desc", defaultValue = "desc")
    private String sortDirection = "desc";
    
    // Constructors
    public TaskSearchRequest() {}
    
    // Getters and Setters
    public String getSearchText() {
        return searchText;
    }
    
    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }
    
    public TaskStatus getStatus() {
        return status;
    }
    
    public void setStatus(TaskStatus status) {
        this.status = status;
    }
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public Long getAuthorId() {
        return authorId;
    }
    
    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }
    
    public LocalDateTime getFromDate() {
        return fromDate;
    }
    
    public void setFromDate(LocalDateTime fromDate) {
        this.fromDate = fromDate;
    }
    
    public LocalDateTime getToDate() {
        return toDate;
    }
    
    public void setToDate(LocalDateTime toDate) {
        this.toDate = toDate;
    }
    
    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
        this.page = page;
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
    }
    
    public String getSortBy() {
        return sortBy;
    }
    
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
    
    public String getSortDirection() {
        return sortDirection;
    }
    
    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
}
