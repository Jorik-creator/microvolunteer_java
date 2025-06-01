package com.microvolunteer.dto.response;

import com.microvolunteer.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private String location;
    private String requiredSkills;
    private Integer maxParticipants;
    private Integer participantCount;
    private TaskStatus status;
    private LocalDateTime scheduledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Додаткові поля для зручності
    private String creatorName;
    private String categoryName;
    private UserResponse creator;
    private CategoryResponse category;
}