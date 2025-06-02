package com.microvolunteer.service;

import com.microvolunteer.dto.response.TaskResponse;
import com.microvolunteer.dto.response.UserResponse;
import com.microvolunteer.entity.Participation;
import com.microvolunteer.entity.Task;
import com.microvolunteer.entity.User;
import com.microvolunteer.enums.TaskStatus;
import com.microvolunteer.enums.UserType;
import com.microvolunteer.exception.BusinessException;
import com.microvolunteer.mapper.TaskMapper;
import com.microvolunteer.mapper.UserMapper;
import com.microvolunteer.repository.ParticipationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for participation operations.
 */
@Service
@Transactional
public class ParticipationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ParticipationService.class);
    
    private final ParticipationRepository participationRepository;
    private final TaskService taskService;
    private final UserService userService;
    private final TaskMapper taskMapper;
    private final UserMapper userMapper;
    
    public ParticipationService(ParticipationRepository participationRepository,
                               TaskService taskService,
                               UserService userService,
                               TaskMapper taskMapper,
                               UserMapper userMapper) {
        this.participationRepository = participationRepository;
        this.taskService = taskService;
        this.userService = userService;
        this.taskMapper = taskMapper;
        this.userMapper = userMapper;
    }
    
    /**
     * Join a task as a volunteer
     */
    public void joinTask(Long taskId, String keycloakSubject) {
        logger.info("User attempting to join task with id: {}", taskId);
        
        Task task = taskService.getTaskEntityById(taskId);
        User volunteer = userService.getUserEntityByKeycloakSubject(keycloakSubject);
        
        // Validate participation eligibility
        validateParticipationEligibility(task, volunteer);
        
        // Check if already participating
        if (participationRepository.existsByVolunteerAndTaskAndActiveTrue(volunteer, task)) {
            throw BusinessException.alreadyParticipating(taskId);
        }
        
        // Create new participation
        Participation participation = new Participation(volunteer, task);
        participationRepository.save(participation);
        
        // Update task status to IN_PROGRESS if it was OPEN
        taskService.updateTaskStatusToInProgress(taskId);
        
        logger.info("User {} successfully joined task {}", volunteer.getId(), taskId);
    }
    
    /**
     * Leave a task
     */
    public void leaveTask(Long taskId, String keycloakSubject) {
        logger.info("User attempting to leave task with id: {}", taskId);
        
        Task task = taskService.getTaskEntityById(taskId);
        User volunteer = userService.getUserEntityByKeycloakSubject(keycloakSubject);
        
        Participation participation = participationRepository
            .findByVolunteerAndTaskAndActiveTrue(volunteer, task)
            .orElseThrow(() -> new BusinessException(
                "User is not participating in this task", 
                "NOT_PARTICIPATING"
            ));
        
        participation.setActive(false);
        participation.setLeftAt(LocalDateTime.now());
        participationRepository.save(participation);
        
        logger.info("User {} successfully left task {}", volunteer.getId(), taskId);
    }
    
    /**
     * Get tasks that user is participating in
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getParticipatingTasks(String keycloakSubject) {
        User volunteer = userService.getUserEntityByKeycloakSubject(keycloakSubject);
        
        List<Participation> participations = participationRepository
            .findActiveParticipationsWithTaskDetails(volunteer);
        
        return participations.stream()
            .map(Participation::getTask)
            .map(taskMapper::toResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get volunteers participating in a task
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getTaskVolunteers(Long taskId) {
        Task task = taskService.getTaskEntityById(taskId);
        
        List<Participation> participations = participationRepository
            .findByTaskAndActiveTrueOrderByJoinedAtAsc(task);
        
        return participations.stream()
            .map(Participation::getVolunteer)
            .map(userMapper::toResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get participation history for a volunteer
     */
    @Transactional(readOnly = true)
    public List<ParticipationHistory> getVolunteerHistory(String keycloakSubject) {
        User volunteer = userService.getUserEntityByKeycloakSubject(keycloakSubject);
        
        List<Participation> participations = participationRepository.findByVolunteer(volunteer);
        
        return participations.stream()
            .map(this::mapToParticipationHistory)
            .collect(Collectors.toList());
    }
    
    /**
     * Get participation statistics for a volunteer
     */
    @Transactional(readOnly = true)
    public VolunteerStatistics getVolunteerStatistics(String keycloakSubject) {
        User volunteer = userService.getUserEntityByKeycloakSubject(keycloakSubject);
        
        long activeParticipations = participationRepository.countByVolunteerAndActiveTrue(volunteer);
        long totalParticipations = participationRepository.findByVolunteer(volunteer).size();
        
        return new VolunteerStatistics(activeParticipations, totalParticipations);
    }
    
    /**
     * Get global volunteer statistics
     */
    @Transactional(readOnly = true)
    public List<VolunteerRanking> getVolunteerRankings() {
        List<Object[]> results = participationRepository.findVolunteerStatistics();
        
        return results.stream()
            .map(result -> {
                User volunteer = (User) result[0];
                Long participationCount = (Long) result[1];
                return new VolunteerRanking(
                    userMapper.toResponse(volunteer),
                    participationCount.intValue()
                );
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Check if user is participating in a task
     */
    @Transactional(readOnly = true)
    public boolean isUserParticipating(Long taskId, String keycloakSubject) {
        Task task = taskService.getTaskEntityById(taskId);
        User volunteer = userService.getUserEntityByKeycloakSubject(keycloakSubject);
        
        return participationRepository.existsByVolunteerAndTaskAndActiveTrue(volunteer, task);
    }
    
    /**
     * Validate if user can participate in task
     */
    private void validateParticipationEligibility(Task task, User volunteer) {
        // Only volunteers can participate
        if (volunteer.getUserType() != UserType.VOLUNTEER) {
            throw BusinessException.unauthorizedAccess("participate in task");
        }
        
        // Task author cannot participate in their own task
        if (task.getAuthor().getId().equals(volunteer.getId())) {
            throw BusinessException.cannotParticipateInOwnTask();
        }
        
        // Task must be open
        if (task.getStatus() != TaskStatus.OPEN && task.getStatus() != TaskStatus.IN_PROGRESS) {
            throw BusinessException.taskNotOpen(task.getId());
        }
        
        // Volunteer must be active
        if (!volunteer.getActive()) {
            throw new BusinessException("Inactive users cannot participate in tasks", "USER_INACTIVE");
        }
    }
    
    /**
     * Map Participation to ParticipationHistory
     */
    private ParticipationHistory mapToParticipationHistory(Participation participation) {
        return new ParticipationHistory(
            participation.getId(),
            taskMapper.toResponse(participation.getTask()),
            participation.getActive(),
            participation.getNotes(),
            participation.getJoinedAt(),
            participation.getLeftAt()
        );
    }
    
    /**
     * Inner class for participation history
     */
    public static class ParticipationHistory {
        private final Long id;
        private final TaskResponse task;
        private final Boolean active;
        private final String notes;
        private final LocalDateTime joinedAt;
        private final LocalDateTime leftAt;
        
        public ParticipationHistory(Long id, TaskResponse task, Boolean active, String notes,
                                   LocalDateTime joinedAt, LocalDateTime leftAt) {
            this.id = id;
            this.task = task;
            this.active = active;
            this.notes = notes;
            this.joinedAt = joinedAt;
            this.leftAt = leftAt;
        }
        
        // Getters
        public Long getId() { return id; }
        public TaskResponse getTask() { return task; }
        public Boolean getActive() { return active; }
        public String getNotes() { return notes; }
        public LocalDateTime getJoinedAt() { return joinedAt; }
        public LocalDateTime getLeftAt() { return leftAt; }
    }
    
    /**
     * Inner class for volunteer statistics
     */
    public static class VolunteerStatistics {
        private final long activeParticipations;
        private final long totalParticipations;
        
        public VolunteerStatistics(long activeParticipations, long totalParticipations) {
            this.activeParticipations = activeParticipations;
            this.totalParticipations = totalParticipations;
        }
        
        public long getActiveParticipations() { return activeParticipations; }
        public long getTotalParticipations() { return totalParticipations; }
    }
    
    /**
     * Inner class for volunteer ranking
     */
    public static class VolunteerRanking {
        private final UserResponse volunteer;
        private final int participationCount;
        
        public VolunteerRanking(UserResponse volunteer, int participationCount) {
            this.volunteer = volunteer;
            this.participationCount = participationCount;
        }
        
        public UserResponse getVolunteer() { return volunteer; }
        public int getParticipationCount() { return participationCount; }
    }
}
