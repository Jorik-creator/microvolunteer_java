package com.microvolunteer.service;

import com.microvolunteer.dto.request.TaskCreateRequest;
import com.microvolunteer.dto.request.TaskSearchRequest;
import com.microvolunteer.dto.response.CategoryResponse;
import com.microvolunteer.dto.response.TaskResponse;
import com.microvolunteer.dto.response.UserResponse;
import com.microvolunteer.entity.Category;
import com.microvolunteer.entity.Task;
import com.microvolunteer.entity.User;
import com.microvolunteer.enums.TaskStatus;
import com.microvolunteer.enums.UserType;
import com.microvolunteer.exception.BusinessException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
    private Task testTask;
    private Category testCategory;
    private TaskCreateRequest testCreateRequest;
    private TaskResponse testTaskResponse;
    private TaskSearchRequest testSearchRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .keycloakId("test-keycloak-id")
                .username("testuser")
                .email("test@example.com")
                .userType(UserType.VULNERABLE)
                .isActive(true)
                .build();

        testCategory = Category.builder()
                .id(1L)
                .name("Допомога з покупками")
                .description("Допомога з покупками продуктів та необхідних речей")
                .build();

        testTask = Task.builder()
                .id(1L)
                .title("Тестове завдання")
                .description("Опис тестового завдання")
                .category(testCategory)
                .creator(testUser)
                .location("Київ")
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(1).plusHours(2))
                //.maxParticipants(3)
                .status(TaskStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();

        testCreateRequest = TaskCreateRequest.builder()
                .title("Тестове завдання")
                .description("Опис тестового завдання")
                .categoryId(1L)
                .location("Київ")
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(1).plusHours(2))
                .maxParticipants(3)
                .build();

        testTaskResponse = TaskResponse.builder()
                .id(1L)
                .title("Тестове завдання")
                .description("Опис тестового завдання")
                .category(CategoryResponse.builder()
                        .id(1L)
                        .name("Допомога з покупками")
                        .description("Допомога з покупками продуктів та необхідних речей")
                        .build())
                .creator(UserResponse.builder()
                        .id(1L)
                        .username("testuser")
                        .userType(UserType.VULNERABLE)
                        .build())
                .location("Київ")
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(1).plusHours(2))
                .maxVolunteers(3)
                .currentVolunteers(0)
                .availableSpots(3)
                .status(TaskStatus.OPEN.name())
                .createdAt(LocalDateTime.now())
                .pastDue(false)
                .canJoin(false)
                .participant(false)
                .build();

        testSearchRequest = TaskSearchRequest.builder()
                .page(0)
                .size(10)
                .build();
    }

    @Test
    @DisplayName("Створення завдання - успішно")
    void createTask_Success() {
        // Given
        when(userRepository.findByKeycloakId("test-keycloak-id")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(taskMapper.toEntity(testCreateRequest)).thenReturn(testTask);
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(taskMapper.toResponse(testTask)).thenReturn(testTaskResponse);

        // When
        TaskResponse result = taskService.createTask("test-keycloak-id", testCreateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Тестове завдання");
        assertThat(result.getCanJoin()).isFalse();
        assertThat(result.getParticipant()).isFalse();

        verify(userRepository).findByKeycloakId("test-keycloak-id");
        verify(categoryRepository).findById(1L);
        verify(taskMapper).toEntity(testCreateRequest);
        verify(taskRepository).save(any(Task.class));
        verify(taskMapper).toResponse(testTask);
    }

    @Test
    @DisplayName("Створення завдання - користувач не знайдений")
    void createTask_UserNotFound() {
        // Given
        when(userRepository.findByKeycloakId("invalid-id")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.createTask("invalid-id", testCreateRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Користувача не знайдено");

        verify(userRepository).findByKeycloakId("invalid-id");
        verifyNoInteractions(categoryRepository, taskRepository, taskMapper);
    }

    @Test
    @DisplayName("Створення завдання - неправильний тип користувача")
    void createTask_WrongUserType() {
        // Given
        testUser.setUserType(UserType.VOLUNTEER);
        when(userRepository.findByKeycloakId("test-keycloak-id")).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> taskService.createTask("test-keycloak-id", testCreateRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Тільки вразливі люди можуть створювати завдання");

        verify(userRepository).findByKeycloakId("test-keycloak-id");
        verifyNoInteractions(categoryRepository, taskRepository, taskMapper);
    }

    @Test
    @DisplayName("Пошук завдань - успішно")
    void searchTasks_Success() {
        // Given
        List<Task> tasks = Arrays.asList(testTask);
        Page<Task> taskPage = new PageImpl<>(tasks);

        when(taskRepository.searchTasks(
                any(), any(), any(), any(), any(), any(Pageable.class)
        )).thenReturn(taskPage);
        when(taskMapper.toResponse(testTask)).thenReturn(testTaskResponse);

        // When
        Page<TaskResponse> result = taskService.searchTasks(testSearchRequest, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Тестове завдання");

        verify(taskRepository).searchTasks(
                any(), any(), any(), any(), any(), any(Pageable.class)
        );
        verify(taskMapper).toResponse(testTask);
    }

    @Test
    @DisplayName("Отримання завдання за ID - успішно")
    void getTaskById_Success() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskMapper.toResponse(testTask)).thenReturn(testTaskResponse);

        // When
        TaskResponse result = taskService.getTaskById(1L, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Тестове завдання");

        verify(taskRepository).findById(1L);
        verify(taskMapper).toResponse(testTask);
    }

    @Test
    @DisplayName("Отримання завдання за ID - не знайдено")
    void getTaskById_NotFound() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.getTaskById(1L, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Завдання не знайдено");

        verify(taskRepository).findById(1L);
        verifyNoInteractions(taskMapper);
    }

    @Test
    @DisplayName("Приєднання до завдання - успішно")
    void joinTask_Success() {
        // Given
        User volunteer = User.builder()
                .id(2L)
                .keycloakId("volunteer-id")
                .userType(UserType.VOLUNTEER)
                .build();

        when(userRepository.findByKeycloakId("volunteer-id")).thenReturn(Optional.of(volunteer));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(participationRepository.existsByTaskIdAndUserId(1L, 2L)).thenReturn(false);
        when(taskMapper.toResponse(testTask)).thenReturn(testTaskResponse);

        // When
        TaskResponse result = taskService.joinTask("volunteer-id", 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getParticipant()).isTrue();
        assertThat(result.getCanJoin()).isFalse();

        verify(userRepository).findByKeycloakId("volunteer-id");
        verify(taskRepository).findById(1L);
        verify(participationRepository).existsByTaskIdAndUserId(1L, 2L);
        verify(participationRepository).save(any());
    }

    @Test
    @DisplayName("Отримання останніх завдань - успішно")
    void getRecentTasks_Success() {
        // Given
        List<Task> tasks = Arrays.asList(testTask);
        when(taskRepository.findTop6ByStatusOrderByCreatedAtDesc(TaskStatus.OPEN)).thenReturn(tasks);
        when(taskMapper.toResponse(testTask)).thenReturn(testTaskResponse);

        // When
        List<TaskResponse> result = taskService.getRecentTasks();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Тестове завдання");

        verify(taskRepository).findTop6ByStatusOrderByCreatedAtDesc(TaskStatus.OPEN);
        verify(taskMapper).toResponse(testTask);
    }
}
