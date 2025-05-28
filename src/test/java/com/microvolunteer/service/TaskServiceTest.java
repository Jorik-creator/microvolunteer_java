package com.microvolunteer.service;

import com.microvolunteer.dto.request.TaskCreateRequest;
import com.microvolunteer.dto.request.TaskSearchRequest;
import com.microvolunteer.dto.response.TaskResponse;
import com.microvolunteer.entity.*;
import com.microvolunteer.enums.TaskStatus;
import com.microvolunteer.enums.UserType;
import com.microvolunteer.exception.ResourceNotFoundException;
import com.microvolunteer.exception.ValidationException;
import com.microvolunteer.mapper.TaskMapper;
import com.microvolunteer.repository.CategoryRepository;
import com.microvolunteer.repository.ParticipationRepository;
import com.microvolunteer.repository.TaskRepository;
import com.microvolunteer.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тести для TaskService")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ParticipationRepository participationRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    private User testUser;
    private Category testCategory;
    private Task testTask;
    private TaskResponse testTaskResponse;
    private TaskCreateRequest testCreateRequest;

    @BeforeEach
    void setUp() {
        // Ініціалізація тестових даних
        testUser = User.builder()
                .id(1L)
                .keycloakId("test-keycloak-id")
                .username("testuser")
                .email("test@example.com")
                .userType(UserType.VOLUNTEER)
                .isActive(true)
                .build();

        testCategory = Category.builder()
                .id(1L)
                .name("Тестова категорія")
                .description("Опис тестової категорії")
                .build();

        testTask = Task.builder()
                .id(1L)
                .title("Тестове завдання")
                .description("Опис тестового завдання")
                .location("Київ")
                .duration(60)
                .maxVolunteers(5)
                .currentVolunteers(0)
                .status(TaskStatus.OPEN)
                .category(testCategory)
                .creator(testUser)
                .deadline(LocalDateTime.now().plusDays(7))
                .build();

        testTaskResponse = new TaskResponse();
        testTaskResponse.setId(1L);
        testTaskResponse.setTitle("Тестове завдання");
        testTaskResponse.setDescription("Опис тестового завдання");
        testTaskResponse.setLocation("Київ");
        testTaskResponse.setDuration(60);
        testTaskResponse.setMaxVolunteers(5);
        testTaskResponse.setCurrentVolunteers(0);
        testTaskResponse.setStatus(TaskStatus.OPEN.name());

        testCreateRequest = new TaskCreateRequest();
        testCreateRequest.setTitle("Нове завдання");
        testCreateRequest.setDescription("Опис нового завдання");
        testCreateRequest.setLocation("Львів");
        testCreateRequest.setDuration(120);
        testCreateRequest.setMaxVolunteers(10);
        testCreateRequest.setCategoryId(1L);
        testCreateRequest.setDeadline(LocalDateTime.now().plusDays(14));
    }

    @Test
    @DisplayName("Створення завдання - успішно")
    void createTask_Success() {
        // Given
        when(userRepository.findByKeycloakId(testUser.getKeycloakId())).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(testCreateRequest.getCategoryId())).thenReturn(Optional.of(testCategory));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(taskMapper.toResponse(testTask)).thenReturn(testTaskResponse);

        // When
        TaskResponse result = taskService.createTask(testUser.getKeycloakId(), testCreateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testTaskResponse.getId());
        assertThat(result.getTitle()).isEqualTo(testTaskResponse.getTitle());
        
        verify(userRepository).findByKeycloakId(testUser.getKeycloakId());
        verify(categoryRepository).findById(testCreateRequest.getCategoryId());
        verify(taskRepository).save(any(Task.class));
        verify(taskMapper).toResponse(testTask);
    }

    @Test
    @DisplayName("Створення завдання - користувач не знайдений")
    void createTask_UserNotFound() {
        // Given
        when(userRepository.findByKeycloakId(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.createTask("invalid-keycloak-id", testCreateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Користувача не знайдено");

        verify(userRepository).findByKeycloakId("invalid-keycloak-id");
        verifyNoInteractions(categoryRepository, taskRepository, taskMapper);
    }

    @Test
    @DisplayName("Створення завдання - категорія не знайдена")
    void createTask_CategoryNotFound() {
        // Given
        when(userRepository.findByKeycloakId(testUser.getKeycloakId())).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(testCreateRequest.getCategoryId())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.createTask(testUser.getKeycloakId(), testCreateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Категорію не знайдено");

        verify(userRepository).findByKeycloakId(testUser.getKeycloakId());
        verify(categoryRepository).findById(testCreateRequest.getCategoryId());
        verifyNoInteractions(taskRepository, taskMapper);
    }

    @Test
    @DisplayName("Пошук завдань - успішно")
    void searchTasks_Success() {
        // Given
        TaskSearchRequest searchRequest = new TaskSearchRequest();
        searchRequest.setPage(0);
        searchRequest.setSize(10);
        searchRequest.setCategoryId(1L);
        searchRequest.setStatus(TaskStatus.OPEN);

        List<Task> tasks = Arrays.asList(testTask);
        Page<Task> taskPage = new PageImpl<>(tasks, PageRequest.of(0, 10), 1);
        Page<TaskResponse> expectedPage = new PageImpl<>(Arrays.asList(testTaskResponse), PageRequest.of(0, 10), 1);

        when(taskRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(taskPage);
        when(taskMapper.toResponse(testTask)).thenReturn(testTaskResponse);

        // When
        Page<TaskResponse> result = taskService.searchTasks(searchRequest, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(testTaskResponse.getId());

        verify(taskRepository).findAll(any(Specification.class), any(Pageable.class));
        verify(taskMapper).toResponse(testTask);
    }

    @Test
    @DisplayName("Отримання завдання за ID - успішно")
    void getTaskById_Success() {
        // Given
        when(taskRepository.findById(testTask.getId())).thenReturn(Optional.of(testTask));
        when(taskMapper.toResponse(testTask)).thenReturn(testTaskResponse);

        // When
        TaskResponse result = taskService.getTaskById(testTask.getId(), null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testTaskResponse.getId());

        verify(taskRepository).findById(testTask.getId());
        verify(taskMapper).toResponse(testTask);
    }

    @Test
    @DisplayName("Отримання завдання за ID - не знайдено")
    void getTaskById_NotFound() {
        // Given
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.getTaskById(999L, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Завдання не знайдено");

        verify(taskRepository).findById(999L);
        verifyNoInteractions(taskMapper);
    }

    @Test
    @DisplayName("Приєднання до завдання - успішно")
    void joinTask_Success() {
        // Given
        testTask.setCurrentVolunteers(2);
        testTask.setMaxVolunteers(5);
        
        when(userRepository.findByKeycloakId(testUser.getKeycloakId())).thenReturn(Optional.of(testUser));
        when(taskRepository.findById(testTask.getId())).thenReturn(Optional.of(testTask));
        when(participationRepository.existsByUserAndTask(testUser, testTask)).thenReturn(false);
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(taskMapper.toResponse(testTask)).thenReturn(testTaskResponse);

        // When
        TaskResponse result = taskService.joinTask(testUser.getKeycloakId(), testTask.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(testTask.getCurrentVolunteers()).isEqualTo(3);

        verify(userRepository).findByKeycloakId(testUser.getKeycloakId());
        verify(taskRepository).findById(testTask.getId());
        verify(participationRepository).existsByUserAndTask(testUser, testTask);
        verify(participationRepository).save(any(Participation.class));
        verify(taskRepository).save(testTask);
    }

    @Test
    @DisplayName("Приєднання до завдання - вже учасник")
    void joinTask_AlreadyParticipating() {
        // Given
        when(userRepository.findByKeycloakId(testUser.getKeycloakId())).thenReturn(Optional.of(testUser));
        when(taskRepository.findById(testTask.getId())).thenReturn(Optional.of(testTask));
        when(participationRepository.existsByUserAndTask(testUser, testTask)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> taskService.joinTask(testUser.getKeycloakId(), testTask.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Ви вже є учасником цього завдання");

        verify(participationRepository, never()).save(any(Participation.class));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Приєднання до завдання - досягнуто максимум волонтерів")
    void joinTask_MaxVolunteersReached() {
        // Given
        testTask.setCurrentVolunteers(5);
        testTask.setMaxVolunteers(5);
        
        when(userRepository.findByKeycloakId(testUser.getKeycloakId())).thenReturn(Optional.of(testUser));
        when(taskRepository.findById(testTask.getId())).thenReturn(Optional.of(testTask));
        when(participationRepository.existsByUserAndTask(testUser, testTask)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> taskService.joinTask(testUser.getKeycloakId(), testTask.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Досягнуто максимальну кількість волонтерів");

        verify(participationRepository, never()).save(any(Participation.class));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Завершення завдання - успішно")
    void completeTask_Success() {
        // Given
        testTask.setStatus(TaskStatus.IN_PROGRESS);
        
        when(userRepository.findByKeycloakId(testUser.getKeycloakId())).thenReturn(Optional.of(testUser));
        when(taskRepository.findById(testTask.getId())).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(taskMapper.toResponse(testTask)).thenReturn(testTaskResponse);

        // When
        TaskResponse result = taskService.completeTask(testUser.getKeycloakId(), testTask.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(testTask.getStatus()).isEqualTo(TaskStatus.COMPLETED);

        verify(taskRepository).save(testTask);
        verify(participationRepository).updateParticipationStatus(testTask.getId(), true);
    }

    @Test
    @DisplayName("Завершення завдання - не власник")
    void completeTask_NotOwner() {
        // Given
        User anotherUser = User.builder()
                .id(2L)
                .keycloakId("another-keycloak-id")
                .username("anotheruser")
                .build();
        
        when(userRepository.findByKeycloakId(anotherUser.getKeycloakId())).thenReturn(Optional.of(anotherUser));
        when(taskRepository.findById(testTask.getId())).thenReturn(Optional.of(testTask));

        // When & Then
        assertThatThrownBy(() -> taskService.completeTask(anotherUser.getKeycloakId(), testTask.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Тільки творець завдання може його завершити");

        verify(taskRepository, never()).save(any(Task.class));
    }
}
