package com.microvolunteer.dto.response;

import com.microvolunteer.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private CategoryResponse category;
    private UserResponse creator;
    private String location;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime deadline;
    private Integer duration;
    private Integer maxVolunteers;
    private Integer currentVolunteers;
    private Integer availableSpots;
    private String status;
    private List<String> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean pastDue;
    private Boolean canJoin;
    private Boolean participant;
}