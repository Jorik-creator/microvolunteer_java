package com.microvolunteer.service;

import com.microvolunteer.entity.Participation;
import com.microvolunteer.entity.Task;
import com.microvolunteer.entity.User;
import com.microvolunteer.enums.TaskStatus;
import com.microvolunteer.exception.BusinessException;
import com.microvolunteer.repository.ParticipationRepository;
import com.microvolunteer.repository.TaskRepository;
import com.microvolunteer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParticipationService {

    private final ParticipationRepository participationRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional
    public Participation joinTask(Long taskId, Long userId, String notes) {
        log.info("User {} attempting to join task {}", userId, taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("Task not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

        validateParticipation(task, user);

        Participation participation = Participation.builder()
                .task(task)
                .user(user)
                .notes(notes)
                .joinedAt(LocalDateTime.now())
                .build();

        return participationRepository.save(participation);
    }

    @Transactional
    public boolean leaveTask(Long taskId, Long userId) {
        log.info("User {} attempting to leave task {}", userId, taskId);

        Optional<Participation> participationOpt = participationRepository
                .findByTaskIdAndUserId(taskId, userId);

        if (participationOpt.isPresent()) {
            Participation participation = participationOpt.get();
            Task task = participation.getTask();
            validateLeaving(task);
            participationRepository.delete(participation);
            return true;
        }

        return false;
    }

    public List<Participation> getTaskParticipants(Long taskId) {
        log.info("Fetching participants for task {}", taskId);
        return participationRepository.findByTaskId(taskId);
    }

    public List<Participation> getUserParticipations(Long userId) {
        log.info("Fetching participations for user {}", userId);
        return participationRepository.findByUserId(userId);
    }

    public boolean isUserParticipating(Long taskId, Long userId) {
        return participationRepository.findByTaskIdAndUserId(taskId, userId).isPresent();
    }

    public long getParticipantCount(Long taskId) {
        return participationRepository.countByTaskId(taskId);
    }

    private void validateParticipation(Task task, User user) {
        if (task.getCreator().getId().equals(user.getId())) {
            throw new BusinessException("Task creator cannot participate in their own task");
        }

        if (task.getStatus() != TaskStatus.OPEN) {
            throw new BusinessException("Cannot join task that is not open");
        }

        if (task.getMaxParticipants() != null) {
            long currentParticipants = participationRepository.countByTaskId(task.getId());
            if (currentParticipants >= task.getMaxParticipants()) {
                throw new BusinessException("Task is full - no available spots");
            }
        }

        if (participationRepository.findByTaskIdAndUserId(task.getId(), user.getId()).isPresent()) {
            throw new BusinessException("User is already participating in this task");
        }

        if (task.getScheduledAt() != null && task.getScheduledAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Cannot join task that has already ended");
        }
    }

    private void validateLeaving(Task task) {
        if (task.getStatus() == TaskStatus.IN_PROGRESS) {
            throw new BusinessException("Cannot leave task that is in progress");
        }
    }
}
