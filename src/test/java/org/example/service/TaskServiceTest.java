package org.example.service;

import org.example.dto.TaskRequest;
import org.example.dto.TaskResponse;
import org.example.exception.BadRequestException;
import org.example.exception.ResourceNotFoundException;
import org.example.exception.UnauthorizedException;
import org.example.model.*;
import org.example.repository.CategoryRepository;
import org.example.repository.TaskRepository;
import org.example.repository.UserRepository;
import org.example.util.EntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @Mock
    private EntityMapper entityMapper;
    
    @InjectMocks
    private TaskService taskService;
    
    private User volunteer;
    private User vulnerable;
    private Category category;
    private Task task;
    private TaskRequest taskRequest;
    private TaskResponse taskResponse;
    
    @BeforeEach
    void setUp() {
        volunteer = User.builder()
                .id(1L)
                .username("volunteer1")
                .email("volunteer@test.com")
                .userType(UserType.VOLUNTEER)
                .build();
        
        vulnerable = User.builder()
                .id(2L)
                .username("vulnerable1")
                .email("vulnerable@test.com")
                .userType(UserType.VULNERABLE)
                .build();
        
        category = Category.builder()
                .id(1L)
                .name("Test Category")
                .description("Test Description")
                .build();
        
        task = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .location("Test Location")
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .maxParticipants(5)
                .status(TaskStatus.OPEN)
                .creator(vulnerable)
                .category(category)
                .participants(new HashSet<>())
                .build();
        
        taskRequest = TaskRequest.builder()
                .title("Test Task")
                .description("Test Description")
                .location("Test Location")
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .maxParticipants(5)
                .categoryId(1L)
                .build();
        
        taskResponse = TaskResponse.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .location("Test Location")
                .maxParticipants(5)
                .currentParticipants(0)
                .status("OPEN")
                .build();
    }
    
    @Test
    void getTaskById_Success() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(entityMapper.toTaskResponse(task)).thenReturn(taskResponse);
        
        TaskResponse result = taskService.getTaskById(1L);
        
        assertNotNull(result);
        assertEquals(taskResponse.getId(), result.getId());
        assertEquals(taskResponse.getTitle(), result.getTitle());
        verify(taskRepository).findById(1L);
        verify(entityMapper).toTaskResponse(task);
    }
    
    @Test
    void getTaskById_NotFound_ThrowsResourceNotFoundException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.getTaskById(1L)
        );
        
        assertEquals("Task not found with id: 1", exception.getMessage());
        verify(taskRepository).findById(1L);
        verify(entityMapper, never()).toTaskResponse(any());
    }
    
    @Test
    void createTask_Success() {
        when(userRepository.findByUsername("vulnerable1")).thenReturn(Optional.of(vulnerable));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(entityMapper.toTask(taskRequest)).thenReturn(task);
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(entityMapper.toTaskResponse(task)).thenReturn(taskResponse);
        
        TaskResponse result = taskService.createTask(taskRequest, "vulnerable1");
        
        assertNotNull(result);
        assertEquals(taskResponse.getTitle(), result.getTitle());
        verify(userRepository).findByUsername("vulnerable1");
        verify(categoryRepository).findById(1L);
        verify(taskRepository).save(any(Task.class));
    }
    
    @Test
    void createTask_UserNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.createTask(taskRequest, "nonexistent")
        );
        
        assertEquals("User not found", exception.getMessage());
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    void createTask_CategoryNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findByUsername("vulnerable1")).thenReturn(Optional.of(vulnerable));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.createTask(taskRequest, "vulnerable1")
        );
        
        assertEquals("Category not found with id: 1", exception.getMessage());
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    void createTask_EndDateBeforeStartDate_ThrowsBadRequestException() {
        taskRequest.setEndDate(LocalDateTime.now().minusDays(1)); // Past date
        when(userRepository.findByUsername("vulnerable1")).thenReturn(Optional.of(vulnerable));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> taskService.createTask(taskRequest, "vulnerable1")
        );
        
        assertEquals("End date cannot be before start date", exception.getMessage());
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    void joinTask_Success() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findByUsername("volunteer1")).thenReturn(Optional.of(volunteer));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(entityMapper.toTaskResponse(task)).thenReturn(taskResponse);
        
        TaskResponse result = taskService.joinTask(1L, "volunteer1");
        
        assertNotNull(result);
        verify(taskRepository).findById(1L);
        verify(userRepository).findByUsername("volunteer1");
        verify(taskRepository).save(task);
    }
    
    @Test
    void joinTask_TaskNotFound_ThrowsResourceNotFoundException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.joinTask(1L, "volunteer1")
        );
        
        assertEquals("Task not found with id: 1", exception.getMessage());
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    void joinTask_UserNotFound_ThrowsResourceNotFoundException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findByUsername("volunteer1")).thenReturn(Optional.empty());
        
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.joinTask(1L, "volunteer1")
        );
        
        assertEquals("User not found", exception.getMessage());
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    void joinTask_NotVolunteer_ThrowsBadRequestException() {
        User admin = User.builder()
                .id(3L)
                .username("admin1")
                .userType(UserType.ADMIN)
                .build();
        
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findByUsername("admin1")).thenReturn(Optional.of(admin));
        
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> taskService.joinTask(1L, "admin1")
        );
        
        assertEquals("Only volunteers can join tasks", exception.getMessage());
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    void joinTask_TaskNotOpen_ThrowsBadRequestException() {
        task.setStatus(TaskStatus.COMPLETED);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findByUsername("volunteer1")).thenReturn(Optional.of(volunteer));
        
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> taskService.joinTask(1L, "volunteer1")
        );
        
        assertEquals("Can only join open tasks", exception.getMessage());
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    void joinTask_OwnTask_ThrowsBadRequestException() {
        task.setCreator(volunteer); // Same user as joining
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findByUsername("volunteer1")).thenReturn(Optional.of(volunteer));
        
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> taskService.joinTask(1L, "volunteer1")
        );
        
        assertEquals("Cannot join your own task", exception.getMessage());
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    void getAllTasks_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(List.of(task));
        
        when(taskRepository.findAll(pageable)).thenReturn(taskPage);
        when(entityMapper.toTaskResponse(task)).thenReturn(taskResponse);
        
        Page<TaskResponse> result = taskService.getAllTasks(null, null, null, null, null, null, pageable);
        
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(taskResponse, result.getContent().get(0));
        verify(taskRepository).findAll(pageable);
    }
    
    @Test
    void deleteTask_Success() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        
        taskService.deleteTask(1L, "vulnerable1");
        
        verify(taskRepository).findById(1L);
        verify(taskRepository).delete(task);
    }
    
    @Test
    void deleteTask_NotOwner_ThrowsUnauthorizedException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> taskService.deleteTask(1L, "volunteer1")
        );
        
        assertEquals("You can only delete your own tasks", exception.getMessage());
        verify(taskRepository, never()).delete(any());
    }
    
    @Test
    void deleteTask_InProgress_ThrowsBadRequestException() {
        task.setStatus(TaskStatus.IN_PROGRESS);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> taskService.deleteTask(1L, "vulnerable1")
        );
        
        assertEquals("Cannot delete task that is in progress", exception.getMessage());
        verify(taskRepository, never()).delete(any());
    }
}