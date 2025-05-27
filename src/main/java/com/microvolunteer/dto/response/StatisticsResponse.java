package com.microvolunteer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsResponse {

    private Long userId;
    private String username;
    private String userType;
    private LocalDateTime dateJoined;
    private Long daysAsMember;

    // Для волонтерів
    private Long totalParticipations;
    private Long completedTasks;
    private Long totalHoursHelped;
    private Long categoriesHelped;
    private Map<String, Long> monthlyActivity;

    // Для вразливих людей
    private Long totalCreatedTasks;
    private Long cancelledTasks;
    private Long totalVolunteersHelped;
    private Map<String, Long> tasksByCategory;
}