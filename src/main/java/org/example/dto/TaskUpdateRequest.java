package org.example.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskUpdateRequest {
    
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
    
    private String location;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    @Min(value = 1, message = "Maximum participants must be at least 1")
    @Max(value = 100, message = "Maximum participants must not exceed 100")
    private Integer maxParticipants;
    
    private Long categoryId;
}