package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatisticsResponse {
    
    private Long userId;
    private String username;
    private String userType;
    private LocalDateTime dateJoined;
    private Long totalCreatedTasks;
    private Long totalCompletedTasks;
    private Long totalCancelledTasks;
    private Long totalParticipatedTasks;
    private Long totalVolunteersHelped;
}