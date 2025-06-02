package com.microvolunteer.service;

import com.microvolunteer.dto.request.TaskCreateRequest;
import com.microvolunteer.dto.request.TaskSearchRequest;
import com.microvolunteer.dto.response.TaskResponse;
import com.microvolunteer.entity.Category;
import com.microvolunteer.entity.Task;
import com.microvolunteer.entity.User;
import com.microvolunteer.enums.TaskStatus;
import com.microvolunteer.enums.UserType;
import com.microvolunteer.exception.BusinessException;
import com.microvolunteer.mapper.TaskMapper;
import com.microvolunteer.repository.ParticipationRepository;
import com.microvolunteer.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ParticipationRepository participationRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private UserService userService;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private TaskService taskService;

    private User sensitiveUser;
    private User volunteerUser;
    private User adminUser;
    private Task task;
    private TaskCreateRequest taskCreateRequest;
    private TaskResponse taskResponse;
    private Category category;

    @BeforeEach
    void setUp() {
        // Setup users
        sensitiveUser = createUser(1L, "sensitive@test.com", UserType.SENSITIVE, "sensitive-subject");
        volunteerUser = createUser(2L, "volunteer@test.com", UserType.VOLUNTEER, "volunteer-subject");
        adminUser = createUser(3L, "admin@test.com", UserType.ADMIN, "admin-subject");

        // Setup category
        category = new Category();
        category.setId(1L);
        category.setName("Test Category");

        // Setup task
        task = createTask(1L, "Test Task", sensitiveUser);

        // Setup DTOs
        taskCreateRequest = new TaskCreateRequest();
        taskCreateRequest.setTitle("Test Task");
        taskCreateRequest.setDescription("Test Description");
        taskCreateRequest.setCategoryIds(Set.of(1L));

        taskResponse = new TaskResponse();
        taskResponse.setId(1L);
        taskResponse.setTitle("Test Task");
    }

    @Nested
    @DisplayName("Create Task Tests")
    class CreateTaskTests {

        @Test
        @DisplayName("Should create task successfully when user is SENSITIVE")
        void shouldCreateTaskSuccessfully() {
            // Given
            when(userService.getUserEntityByKeycloakSubject("sensitive-subject"))
                    .thenReturn(sensitiveUser);
            when(taskMapper.toEntity(taskCreateRequest)).thenReturn(task);
            when(categoryService.getCategoriesByIds(Set.of(1L))).thenReturn(Set.of(category));
            when(taskRepository.save(any(Task.class))).thenReturn(task);
            when(taskMapper.toResponse(task)).thenReturn(taskResponse);
            when(participationRepository.countByTaskAndActiveTrue(task)).thenReturn(0L);

            // When
            TaskResponse result = taskService.createTask(taskCreateRequest, "sensitive-subject");

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("Test Task", result.getTitle());
            
            verify(userService).getUserEntityByKeycloakSubject("sensitive-subject");
            verify(taskRepository).save(any(Task.class));
            verify(taskMapper).toEntity(taskCreateRequest);
            verify(categoryService).getCategoriesByIds(Set.of(1L));
        }

        @Test
        @DisplayName("Should throw exception when user is not SENSITIVE")
        void shouldThrowExceptionWhenUserNotSensitive() {
            // Given
            when(userService.getUserEntityByKeycloakSubject("volunteer-subject"))
                    .thenReturn(volunteerUser);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> taskService.createTask(taskCreateRequest, "volunteer-subject"));

            assertEquals("UNAUTHORIZED_ACCESS", exception.getCode());
            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("Should create task without categories when none provided")
        void shouldCreateTaskWithoutCategories() {
            // Given
            taskCreateRequest.setCategoryIds(null);
            when(userService.getUserEntityByKeycloakSubject("sensitive-subject"))
                    .thenReturn(sensitiveUser);
            when(taskMapper.toEntity(taskCreateRequest)).thenReturn(task);
            when(taskRepository.save(any(Task.class))).thenReturn(task);
            when(taskMapper.toResponse(task)).thenReturn(taskResponse);
            when(participationRepository.countByTaskAndActiveTrue(task)).thenReturn(0L);

            // When
            TaskResponse result = taskService.createTask(taskCreateRequest, "sensitive-subject");

            // Then
            assertNotNull(result);
            verify(categoryService, never()).getCategoriesByIds(any());
            verify(taskRepository).save(any(Task.class));
        }
    }

    @Nested
    @DisplayName("Get Task Tests")
    class GetTaskTests {

        @Test
        @DisplayName("Should get task by ID successfully")
        void shouldGetTaskByIdSuccessfully() {
            // Given
            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
            when(taskMapper.toResponse(task)).thenReturn(taskResponse);
            when(participationRepository.countByTaskAndActiveTrue(task)).thenReturn(2L);

            // When
            TaskResponse result = taskService.getTaskById(1L);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getId());
            verify(taskRepository).findById(1L);
            verify(taskMapper).toResponse(task);
        }

        @Test
        @DisplayName("Should throw exception when task not found")
        void shouldThrowExceptionWhenTaskNotFound() {
            // Given
            when(taskRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> taskService.getTaskById(1L));

            assertEquals("TASK_NOT_FOUND", exception.getCode());
        }

        @Test
        @DisplayName("Should get tasks by author successfully")
        void shouldGetTasksByAuthorSuccessfully() {
            // Given
            List<Task> tasks = List.of(task);
            when(userService.getUserEntityByKeycloakSubject("sensitive-subject"))
                    .thenReturn(sensitiveUser);
            when(taskRepository.findByAuthor(sensitiveUser)).thenReturn(tasks);
            when(taskMapper.toResponse(task)).thenReturn(taskResponse);
            when(participationRepository.countByTaskAndActiveTrue(task)).thenReturn(0L);

            // When
            List<TaskResponse> result = taskService.getTasksByAuthor("sensitive-subject");

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getId());
        }

        @Test
        @DisplayName("Should get tasks by status successfully")
        void shouldGetTasksByStatusSuccessfully() {
            // Given
            List<Task> tasks = List.of(task);
            when(taskRepository.findByStatus(TaskStatus.OPEN)).thenReturn(tasks);
            when(taskMapper.toResponse(task)).thenReturn(taskResponse);
            when(participationRepository.countByTaskAndActiveTrue(task)).thenReturn(0L);

            // When
            List<TaskResponse> result = taskService.getTasksByStatus(TaskStatus.OPEN);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("Search Tasks Tests")
    class SearchTasksTests {

        @Test
        @DisplayName("Should search tasks with filters and pagination")
        void shouldSearchTasksWithFilters() {
            // Given
            TaskSearchRequest searchRequest = new TaskSearchRequest();
            searchRequest.setPage(0);
            searchRequest.setSize(10);
            searchRequest.setSortBy("createdAt");
            searchRequest.setSortDirection("desc");
            searchRequest.setStatus(TaskStatus.OPEN);

            Pageable pageable = PageRequest.of(0, 10);
            Page<Task> tasksPage = new PageImpl<>(List.of(task), pageable, 1);

            when(taskRepository.findTasksWithFilters(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(tasksPage);
            when(taskMapper.toResponse(task)).thenReturn(taskResponse);
            when(participationRepository.countByTaskAndActiveTrue(task)).thenReturn(0L);

            // When
            Page<TaskResponse> result = taskService.searchTasks(searchRequest);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(1, result.getContent().size());
        }
    }

    @Nested
    @DisplayName("Complete Task Tests")
    class CompleteTaskTests {

        @Test
        @DisplayName("Should complete task successfully when user is author")
        void shouldCompleteTaskSuccessfully() {
            // Given
            task.setStatus(TaskStatus.IN_PROGRESS);
            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
            when(userService.getUserEntityByKeycloakSubject("sensitive-subject"))
                    .thenReturn(sensitiveUser);
            when(taskRepository.save(any(Task.class))).thenReturn(task);
            when(taskMapper.toResponse(task)).thenReturn(taskResponse);
            when(participationRepository.countByTaskAndActiveTrue(task)).thenReturn(0L);

            // When
            TaskResponse result = taskService.completeTask(1L, "sensitive-subject");

            // Then
            assertNotNull(result);
            assertEquals(TaskStatus.COMPLETED, task.getStatus());
            assertNotNull(task.getCompletedAt());
            verify(taskRepository).save(task);
        }

        @Test
        @DisplayName("Should throw exception when user is not author")
        void shouldThrowExceptionWhenUserNotAuthor() {
            // Given
            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
            when(userService.getUserEntityByKeycloakSubject("volunteer-subject"))
                    .thenReturn(volunteerUser);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> taskService.completeTask(1L, "volunteer-subject"));

            assertEquals("UNAUTHORIZED_ACCESS", exception.getCode());
            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("Should throw exception when task already completed")
        void shouldThrowExceptionWhenTaskAlreadyCompleted() {
            // Given
            task.setStatus(TaskStatus.COMPLETED);
            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
            when(userService.getUserEntityByKeycloakSubject("sensitive-subject"))
                    .thenReturn(sensitiveUser);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> taskService.completeTask(1L, "sensitive-subject"));

            assertEquals("INVALID_TASK_STATUS", exception.getCode());
        }
    }

    @Nested
    @DisplayName("Delete Task Tests")
    class DeleteTaskTests {

        @Test
        @DisplayName("Should delete task successfully when user is author and no active participants")
        void shouldDeleteTaskSuccessfully() {
            // Given
            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
            when(userService.getUserEntityByKeycloakSubject("sensitive-subject"))
                    .thenReturn(sensitiveUser);
            when(participationRepository.countByTaskAndActiveTrue(task)).thenReturn(0L);

            // When
            taskService.deleteTask(1L, "sensitive-subject");

            // Then
            verify(taskRepository).delete(task);
        }

        @Test
        @DisplayName("Should delete task successfully when user is admin")
        void shouldDeleteTaskSuccessfullyWhenAdmin() {
            // Given
            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
            when(userService.getUserEntityByKeycloakSubject("admin-subject"))
                    .thenReturn(adminUser);
            when(participationRepository.countByTaskAndActiveTrue(task)).thenReturn(0L);

            // When
            taskService.deleteTask(1L, "admin-subject");

            // Then
            verify(taskRepository).delete(task);
        }

        @Test
        @DisplayName("Should throw exception when user not authorized")
        void shouldThrowExceptionWhenUserNotAuthorized() {
            // Given
            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
            when(userService.getUserEntityByKeycloakSubject("volunteer-subject"))
                    .thenReturn(volunteerUser);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> taskService.deleteTask(1L, "volunteer-subject"));

            assertEquals("UNAUTHORIZED_ACCESS", exception.getCode());
            verify(taskRepository, never()).delete(any(Task.class));
        }

        @Test
        @DisplayName("Should throw exception when task has active participants")
        void shouldThrowExceptionWhenTaskHasActiveParticipants() {
            // Given
            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
            when(userService.getUserEntityByKeycloakSubject("sensitive-subject"))
                    .thenReturn(sensitiveUser);
            when(participationRepository.countByTaskAndActiveTrue(task)).thenReturn(2L);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> taskService.deleteTask(1L, "sensitive-subject"));

            assertEquals("TASK_HAS_PARTICIPANTS", exception.getCode());
            verify(taskRepository, never()).delete(any(Task.class));
        }
    }

    @Nested
    @DisplayName("Task Statistics Tests")
    class TaskStatisticsTests {

        @Test
        @DisplayName("Should get task statistics successfully")
        void shouldGetTaskStatisticsSuccessfully() {
            // Given
            when(taskRepository.count()).thenReturn(10L);
            when(taskRepository.countByStatus(TaskStatus.OPEN)).thenReturn(3L);
            when(taskRepository.countByStatus(TaskStatus.IN_PROGRESS)).thenReturn(4L);
            when(taskRepository.countByStatus(TaskStatus.COMPLETED)).thenReturn(3L);

            // When
            TaskService.TaskStatistics result = taskService.getTaskStatistics();

            // Then
            assertNotNull(result);
            assertEquals(10L, result.getTotal());
            assertEquals(3L, result.getOpen());
            assertEquals(4L, result.getInProgress());
            assertEquals(3L, result.getCompleted());
        }
    }

    @Nested
    @DisplayName("Recent Tasks Tests")
    class RecentTasksTests {

        @Test
        @DisplayName("Should get recent tasks successfully")
        void shouldGetRecentTasksSuccessfully() {
            // Given
            List<Task> tasks = List.of(task);
            when(taskRepository.findRecentTasks(any(LocalDateTime.class))).thenReturn(tasks);
            when(taskMapper.toResponse(task)).thenReturn(taskResponse);
            when(participationRepository.countByTaskAndActiveTrue(task)).thenReturn(0L);

            // When
            List<TaskResponse> result = taskService.getRecentTasks(7);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(taskRepository).findRecentTasks(any(LocalDateTime.class));
        }
    }

    @Nested
    @DisplayName("Update Status Tests")
    class UpdateStatusTests {

        @Test
        @DisplayName("Should update task status to IN_PROGRESS when status is OPEN")
        void shouldUpdateTaskStatusToInProgress() {
            // Given
            task.setStatus(TaskStatus.OPEN);
            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
            when(taskRepository.save(task)).thenReturn(task);

            // When
            taskService.updateTaskStatusToInProgress(1L);

            // Then
            assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
            verify(taskRepository).save(task);
        }

        @Test
        @DisplayName("Should not update task status when not OPEN")
        void shouldNotUpdateTaskStatusWhenNotOpen() {
            // Given
            task.setStatus(TaskStatus.IN_PROGRESS);
            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

            // When
            taskService.updateTaskStatusToInProgress(1L);

            // Then
            assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
            verify(taskRepository, never()).save(task);
        }
    }

    // Helper methods
    private User createUser(Long id, String email, UserType userType, String keycloakSubject) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setUserType(userType);
        user.setKeycloakSubject(keycloakSubject);
        user.setActive(true);
        return user;
    }

    private Task createTask(Long id, String title, User author) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setDescription("Test Description");
        task.setAuthor(author);
        task.setStatus(TaskStatus.OPEN);
        task.setCreatedAt(LocalDateTime.now());
        return task;
    }
}
