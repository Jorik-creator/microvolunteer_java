package com.microvolunteer.service;

import com.microvolunteer.entity.Participation;
import com.microvolunteer.entity.Task;
import com.microvolunteer.entity.User;
import com.microvolunteer.enums.TaskStatus;
import com.microvolunteer.exception.BusinessException;
import com.microvolunteer.repository.ParticipationRepository;
import com.microvolunteer.repository.TaskRepository;
import com.microvolunteer.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipationServiceTest {

    @Mock
    private ParticipationRepository participationRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ParticipationService participationService;

    @Test
    void joinTask_ValidRequest_ShouldReturnParticipation() {
        // Given
        Long taskId = 1L;
        Long userId = 1L;
        String notes = "Test notes";

        User creator = new User();
        creator.setId(2L); // Different from participant

        Task task = new Task();
        task.setId(taskId);
        task.setStatus(TaskStatus.OPEN);
        task.setMaxParticipants(5);
        task.setCreator(creator);
        task.setScheduledAt(LocalDateTime.now().plusDays(1));

        User user = new User();
        user.setId(userId);

        Participation participation = new Participation();
        participation.setId(1L);
        participation.setTask(task);
        participation.setUser(user);
        participation.setJoinedAt(LocalDateTime.now());

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(participationRepository.findByTaskIdAndUserId(taskId, userId)).thenReturn(Optional.empty());
        when(participationRepository.countByTaskId(taskId)).thenReturn(2L);
        when(participationRepository.save(any(Participation.class))).thenReturn(participation);

        // When
        Participation result = participationService.joinTask(taskId, userId, notes);

        // Then
        assertNotNull(result);
        assertEquals(taskId, result.getTask().getId());
        assertEquals(userId, result.getUser().getId());
        
        verify(taskRepository).findById(taskId);
        verify(userRepository).findById(userId);
        verify(participationRepository).findByTaskIdAndUserId(taskId, userId);
        verify(participationRepository).save(any(Participation.class));
    }

    @Test
    void joinTask_TaskNotFound_ShouldThrowException() {
        // Given
        Long taskId = 999L;
        Long userId = 1L;
        String notes = "Test notes";

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> participationService.joinTask(taskId, userId, notes)
        );

        assertEquals("Task not found", exception.getMessage());
        verify(taskRepository).findById(taskId);
        verify(participationRepository, never()).save(any(Participation.class));
    }

    @Test
    void joinTask_UserNotFound_ShouldThrowException() {
        // Given
        Long taskId = 1L;
        Long userId = 999L;
        String notes = "Test notes";

        Task task = new Task();
        task.setId(taskId);
        task.setStatus(TaskStatus.OPEN);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> participationService.joinTask(taskId, userId, notes)
        );

        assertEquals("User not found", exception.getMessage());
        verify(participationRepository, never()).save(any(Participation.class));
    }

    @Test
    void joinTask_TaskFull_ShouldThrowException() {
        // Given
        Long taskId = 1L;
        Long userId = 1L;
        String notes = "Test notes";

        User creator = new User();
        creator.setId(2L);

        Task task = new Task();
        task.setId(taskId);
        task.setStatus(TaskStatus.OPEN);
        task.setMaxParticipants(2);
        task.setCreator(creator);
        task.setScheduledAt(LocalDateTime.now().plusDays(1));

        User user = new User();
        user.setId(userId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(participationRepository.findByTaskIdAndUserId(taskId, userId)).thenReturn(Optional.empty());
        when(participationRepository.countByTaskId(taskId)).thenReturn(2L);

        // When & Then
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> participationService.joinTask(taskId, userId, notes)
        );

        assertEquals("Task is full - no available spots", exception.getMessage());
        verify(participationRepository, never()).save(any(Participation.class));
    }

    @Test
    void joinTask_UserIsCreator_ShouldThrowException() {
        // Given
        Long taskId = 1L;
        Long userId = 1L;
        String notes = "Test notes";

        User user = new User();
        user.setId(userId);

        Task task = new Task();
        task.setId(taskId);
        task.setStatus(TaskStatus.OPEN);
        task.setCreator(user); // User is the creator

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When & Then
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> participationService.joinTask(taskId, userId, notes)
        );

        assertEquals("Task creator cannot participate in their own task", exception.getMessage());
        verify(participationRepository, never()).save(any(Participation.class));
    }

    @Test
    void leaveTask_ValidRequest_ShouldReturnTrue() {
        // Given
        Long taskId = 1L;
        Long userId = 1L;

        Task task = new Task();
        task.setStatus(TaskStatus.OPEN); // Not in progress

        Participation participation = new Participation();
        participation.setId(1L);
        participation.setTask(task);

        when(participationRepository.findByTaskIdAndUserId(taskId, userId)).thenReturn(Optional.of(participation));

        // When
        boolean result = participationService.leaveTask(taskId, userId);

        // Then
        assertTrue(result);
        verify(participationRepository).findByTaskIdAndUserId(taskId, userId);
        verify(participationRepository).delete(participation);
    }

    @Test
    void leaveTask_NotParticipating_ShouldReturnFalse() {
        // Given
        Long taskId = 1L;
        Long userId = 1L;

        when(participationRepository.findByTaskIdAndUserId(taskId, userId)).thenReturn(Optional.empty());

        // When
        boolean result = participationService.leaveTask(taskId, userId);

        // Then
        assertFalse(result);
        verify(participationRepository, never()).delete(any(Participation.class));
    }

    @Test
    void getUserParticipations_ShouldReturnParticipationList() {
        // Given
        Long userId = 1L;
        Participation participation1 = new Participation();
        participation1.setId(1L);
        Participation participation2 = new Participation();
        participation2.setId(2L);

        List<Participation> participations = Arrays.asList(participation1, participation2);
        when(participationRepository.findByUserId(userId)).thenReturn(participations);

        // When
        List<Participation> result = participationService.getUserParticipations(userId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        verify(participationRepository).findByUserId(userId);
    }

    @Test
    void getTaskParticipants_ShouldReturnParticipationList() {
        // Given
        Long taskId = 1L;
        Participation participation = new Participation();
        participation.setId(1L);

        List<Participation> participations = Arrays.asList(participation);
        when(participationRepository.findByTaskId(taskId)).thenReturn(participations);

        // When
        List<Participation> result = participationService.getTaskParticipants(taskId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(participationRepository).findByTaskId(taskId);
    }

    @Test
    void isUserParticipating_UserIsParticipating_ShouldReturnTrue() {
        // Given
        Long taskId = 1L;
        Long userId = 1L;

        Participation participation = new Participation();
        when(participationRepository.findByTaskIdAndUserId(taskId, userId)).thenReturn(Optional.of(participation));

        // When
        boolean result = participationService.isUserParticipating(taskId, userId);

        // Then
        assertTrue(result);
        verify(participationRepository).findByTaskIdAndUserId(taskId, userId);
    }

    @Test
    void isUserParticipating_UserIsNotParticipating_ShouldReturnFalse() {
        // Given
        Long taskId = 1L;
        Long userId = 1L;

        when(participationRepository.findByTaskIdAndUserId(taskId, userId)).thenReturn(Optional.empty());

        // When
        boolean result = participationService.isUserParticipating(taskId, userId);

        // Then
        assertFalse(result);
        verify(participationRepository).findByTaskIdAndUserId(taskId, userId);
    }

    @Test
    void getParticipantCount_ShouldReturnCount() {
        // Given
        Long taskId = 1L;
        long expectedCount = 3L;

        when(participationRepository.countByTaskId(taskId)).thenReturn(expectedCount);

        // When
        long result = participationService.getParticipantCount(taskId);

        // Then
        assertEquals(expectedCount, result);
        verify(participationRepository).countByTaskId(taskId);
    }
}
