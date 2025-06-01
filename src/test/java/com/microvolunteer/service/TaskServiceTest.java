package com.microvolunteer.service;

import com.microvolunteer.entity.Task;
import com.microvolunteer.enums.TaskStatus;
import com.microvolunteer.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void getAllTasks_ShouldReturnListOfTasks() {
        // Given
        Task task = createTestTask();
        when(taskRepository.findAll()).thenReturn(List.of(task));

        // When
        List<Task> result = taskService.getAllTasks();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(task, result.get(0));
        verify(taskRepository).findAll();
    }

    @Test
    void getTaskById_WhenTaskExists_ShouldReturnTask() {
        // Given
        Long taskId = 1L;
        Task task = createTestTask();
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        // When
        Optional<Task> result = taskService.getTaskById(taskId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(task, result.get());
        verify(taskRepository).findById(taskId);
    }

    @Test
    void getTaskById_WhenTaskDoesNotExist_ShouldReturnEmpty() {
        // Given
        Long taskId = 1L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // When
        Optional<Task> result = taskService.getTaskById(taskId);

        // Then
        assertTrue(result.isEmpty());
        verify(taskRepository).findById(taskId);
    }

    @Test
    void createTask_ShouldSetStatusAndTimestamps() {
        // Given
        Task task = createTestTask();
        task.setStatus(null);
        task.setCreatedAt(null);
        task.setUpdatedAt(null);
        
        Task savedTask = createTestTask();
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        // When
        Task result = taskService.createTask(task);

        // Then
        assertNotNull(result);
        verify(taskRepository).save(argThat(t -> 
            t.getStatus() == TaskStatus.OPEN &&
            t.getCreatedAt() != null &&
            t.getUpdatedAt() != null
        ));
    }

    @Test
    void deleteTask_WhenTaskExists_ShouldReturnTrue() {
        // Given
        Long taskId = 1L;
        when(taskRepository.existsById(taskId)).thenReturn(true);

        // When
        boolean result = taskService.deleteTask(taskId);

        // Then
        assertTrue(result);
        verify(taskRepository).existsById(taskId);
        verify(taskRepository).deleteById(taskId);
    }

    @Test
    void deleteTask_WhenTaskDoesNotExist_ShouldReturnFalse() {
        // Given
        Long taskId = 1L;
        when(taskRepository.existsById(taskId)).thenReturn(false);

        // When
        boolean result = taskService.deleteTask(taskId);

        // Then
        assertFalse(result);
        verify(taskRepository).existsById(taskId);
        verify(taskRepository, never()).deleteById(taskId);
    }

    private Task createTestTask() {
        return Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
