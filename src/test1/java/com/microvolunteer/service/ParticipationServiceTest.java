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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParticipationService Unit Tests")
class ParticipationServiceTest {

    @Mock
    private ParticipationRepository participationRepository;

    @Mock
    private TaskService taskService;

    @Mock
    private UserService userService;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ParticipationService participationService;

    private User volunteer;
    private User sensitiveUser;
    private User inactiveUser;
    private Task openTask;
    private Task completedTask;
    private Participation participation;
    private TaskResponse taskResponse;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        // Setup users
        volunteer = createUser(1L, "volunteer@test.com", UserType.VOLUNTEER, "volunteer-subject", true);
        sensitiveUser = createUser(2L, "sensitive@test.com", UserType.SENSITIVE, "sensitive-subject", true);
        inactiveUser = createUser(3L, "inactive@test.com", UserType.VOLUNTEER, "inactive-subject", false);

        // Setup tasks
        openTask = createTask(1L, "Open Task", sensitiveUser, TaskStatus.OPEN);
        completedTask = createTask(2L, "Completed Task", sensitiveUser, TaskStatus.COMPLETED);

        // Setup participation
        participation = new Participation(volunteer, openTask);
        participation.setId(1L);

        // Setup DTOs
        taskResponse = new TaskResponse();
        taskResponse.setId(1L);
        taskResponse.setTitle("Open Task");

        userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setEmail("volunteer@test.com");
    }

    @Nested
    @DisplayName("Join Task Tests")
    class JoinTaskTests {

        @Test
        @DisplayName("Should join task successfully when eligible")
        void shouldJoinTaskSuccessfully() {
            // Given
            when(taskService.getTaskEntityById(1L)).thenReturn(openTask);
            when(userService.getUserEntityByKeycloakSubject("volunteer-subject")).thenReturn(volunteer);
            when(participationRepository.existsByVolunteerAndTaskAndActiveTrue(volunteer, openTask))
                    .thenReturn(false);
            when(participationRepository.save(any(Participation.class))).thenReturn(participation);

            // When
            participationService.joinTask(1L, "volunteer-subject");

            // Then
            verify(participationRepository).save(any(Participation.class));
            verify(taskService).updateTaskStatusToInProgress(1L);
        }

        @Test
        @DisplayName("Should throw exception when user is not volunteer")
        void shouldThrowExceptionWhenUserNotVolunteer() {
            // Given
            when(taskService.getTaskEntityById(1L)).thenReturn(openTask);
            when(userService.getUserEntityByKeycloakSubject("sensitive-subject")).thenReturn(sensitiveUser);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> participationService.joinTask(1L, "sensitive-subject"));

            assertEquals("UNAUTHORIZED_ACCESS", exception.getCode());
            verify(participationRepository, never()).save(any(Participation.class));
        }

        @Test
        @DisplayName("Should throw exception when user is task author")
        void shouldThrowExceptionWhenUserIsTaskAuthor() {
            // Given
            User volunteerAuthor = createUser(4L, "volunteer.author@test.com", 
                    UserType.VOLUNTEER, "volunteer-author-subject", true);
            Task taskByVolunteer = createTask(3L, "Task by Volunteer", volunteerAuthor, TaskStatus.OPEN);

            when(taskService.getTaskEntityById(3L)).thenReturn(taskByVolunteer);
            when(userService.getUserEntityByKeycloakSubject("volunteer-author-subject"))
                    .thenReturn(volunteerAuthor);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> participationService.joinTask(3L, "volunteer-author-subject"));

            assertEquals("CANNOT_PARTICIPATE_IN_OWN_TASK", exception.getCode());
            verify(participationRepository, never()).save(any(Participation.class));
        }

        @Test
        @DisplayName("Should throw exception when task is not open")
        void shouldThrowExceptionWhenTaskNotOpen() {
            // Given
            when(taskService.getTaskEntityById(2L)).thenReturn(completedTask);
            when(userService.getUserEntityByKeycloakSubject("volunteer-subject")).thenReturn(volunteer);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> participationService.joinTask(2L, "volunteer-subject"));

            assertEquals("TASK_NOT_OPEN", exception.getCode());
            verify(participationRepository, never()).save(any(Participation.class));
        }

        @Test
        @DisplayName("Should throw exception when user is inactive")
        void shouldThrowExceptionWhenUserInactive() {
            // Given
            when(taskService.getTaskEntityById(1L)).thenReturn(openTask);
            when(userService.getUserEntityByKeycloakSubject("inactive-subject")).thenReturn(inactiveUser);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> participationService.joinTask(1L, "inactive-subject"));

            assertEquals("USER_INACTIVE", exception.getCode());
            verify(participationRepository, never()).save(any(Participation.class));
        }

        @Test
        @DisplayName("Should throw exception when already participating")
        void shouldThrowExceptionWhenAlreadyParticipating() {
            // Given
            when(taskService.getTaskEntityById(1L)).thenReturn(openTask);
            when(userService.getUserEntityByKeycloakSubject("volunteer-subject")).thenReturn(volunteer);
            when(participationRepository.existsByVolunteerAndTaskAndActiveTrue(volunteer, openTask))
                    .thenReturn(true);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> participationService.joinTask(1L, "volunteer-subject"));

            assertEquals("ALREADY_PARTICIPATING", exception.getCode());
            verify(participationRepository, never()).save(any(Participation.class));
        }
    }

    @Nested
    @DisplayName("Leave Task Tests")
    class LeaveTaskTests {

        @Test
        @DisplayName("Should leave task successfully")
        void shouldLeaveTaskSuccessfully() {
            // Given
            when(taskService.getTaskEntityById(1L)).thenReturn(openTask);
            when(userService.getUserEntityByKeycloakSubject("volunteer-subject")).thenReturn(volunteer);
            when(participationRepository.findByVolunteerAndTaskAndActiveTrue(volunteer, openTask))
                    .thenReturn(Optional.of(participation));
            when(participationRepository.save(participation)).thenReturn(participation);

            // When
            participationService.leaveTask(1L, "volunteer-subject");

            // Then
            assertFalse(participation.getActive());
            assertNotNull(participation.getLeftAt());
            verify(participationRepository).save(participation);
        }

        @Test
        @DisplayName("Should throw exception when not participating")
        void shouldThrowExceptionWhenNotParticipating() {
            // Given
            when(taskService.getTaskEntityById(1L)).thenReturn(openTask);
            when(userService.getUserEntityByKeycloakSubject("volunteer-subject")).thenReturn(volunteer);
            when(participationRepository.findByVolunteerAndTaskAndActiveTrue(volunteer, openTask))
                    .thenReturn(Optional.empty());

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> participationService.leaveTask(1L, "volunteer-subject"));

            assertEquals("NOT_PARTICIPATING", exception.getCode());
            verify(participationRepository, never()).save(any(Participation.class));
        }
    }

    @Nested
    @DisplayName("Get Participating Tasks Tests")
    class GetParticipatingTasksTests {

        @Test
        @DisplayName("Should get participating tasks successfully")
        void shouldGetParticipatingTasksSuccessfully() {
            // Given
            List<Participation> participations = List.of(participation);
            when(userService.getUserEntityByKeycloakSubject("volunteer-subject")).thenReturn(volunteer);
            when(participationRepository.findActiveParticipationsWithTaskDetails(volunteer))
                    .thenReturn(participations);
            when(taskMapper.toResponse(openTask)).thenReturn(taskResponse);

            // When
            List<TaskResponse> result = participationService.getParticipatingTasks("volunteer-subject");

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getId());
            verify(taskMapper).toResponse(openTask);
        }

        @Test
        @DisplayName("Should return empty list when no participations")
        void shouldReturnEmptyListWhenNoParticipations() {
            // Given
            when(userService.getUserEntityByKeycloakSubject("volunteer-subject")).thenReturn(volunteer);
            when(participationRepository.findActiveParticipationsWithTaskDetails(volunteer))
                    .thenReturn(List.of());

            // When
            List<TaskResponse> result = participationService.getParticipatingTasks("volunteer-subject");

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Get Task Volunteers Tests")
    class GetTaskVolunteersTests {

        @Test
        @DisplayName("Should get task volunteers successfully")
        void shouldGetTaskVolunteersSuccessfully() {
            // Given
            List<Participation> participations = List.of(participation);
            when(taskService.getTaskEntityById(1L)).thenReturn(openTask);
            when(participationRepository.findByTaskAndActiveTrueOrderByJoinedAtAsc(openTask))
                    .thenReturn(participations);
            when(userMapper.toResponse(volunteer)).thenReturn(userResponse);

            // When
            List<UserResponse> result = participationService.getTaskVolunteers(1L);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getId());
            verify(userMapper).toResponse(volunteer);
        }

        @Test
        @DisplayName("Should return empty list when no volunteers")
        void shouldReturnEmptyListWhenNoVolunteers() {
            // Given
            when(taskService.getTaskEntityById(1L)).thenReturn(openTask);
            when(participationRepository.findByTaskAndActiveTrueOrderByJoinedAtAsc(openTask))
                    .thenReturn(List.of());

            // When
            List<UserResponse> result = participationService.getTaskVolunteers(1L);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Volunteer History Tests")
    class VolunteerHistoryTests {

        @Test
        @DisplayName("Should get volunteer history successfully")
        void shouldGetVolunteerHistorySuccessfully() {
            // Given
            List<Participation> participations = List.of(participation);
            when(userService.getUserEntityByKeycloakSubject("volunteer-subject")).thenReturn(volunteer);
            when(participationRepository.findByVolunteer(volunteer)).thenReturn(participations);
            when(taskMapper.toResponse(openTask)).thenReturn(taskResponse);

            // When
            List<ParticipationService.ParticipationHistory> result = 
                    participationService.getVolunteerHistory("volunteer-subject");

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getId());
            assertNotNull(result.get(0).getTask());
            assertTrue(result.get(0).getActive());
        }
    }

    @Nested
    @DisplayName("Volunteer Statistics Tests")
    class VolunteerStatisticsTests {

        @Test
        @DisplayName("Should get volunteer statistics successfully")
        void shouldGetVolunteerStatisticsSuccessfully() {
            // Given
            when(userService.getUserEntityByKeycloakSubject("volunteer-subject")).thenReturn(volunteer);
            when(participationRepository.countByVolunteerAndActiveTrue(volunteer)).thenReturn(2L);
            when(participationRepository.findByVolunteer(volunteer))
                    .thenReturn(List.of(participation, participation));

            // When
            ParticipationService.VolunteerStatistics result = 
                    participationService.getVolunteerStatistics("volunteer-subject");

            // Then
            assertNotNull(result);
            assertEquals(2L, result.getActiveParticipations());
            assertEquals(2L, result.getTotalParticipations());
        }
    }

    @Nested
    @DisplayName("Volunteer Rankings Tests")
    class VolunteerRankingsTests {

        @Test
        @DisplayName("Should get volunteer rankings successfully")
        void shouldGetVolunteerRankingsSuccessfully() {
            // Given
            Object[] result1 = {volunteer, 5L};
            List<Object[]> statisticsResults = List.of(result1);
            when(participationRepository.findVolunteerStatistics()).thenReturn(statisticsResults);
            when(userMapper.toResponse(volunteer)).thenReturn(userResponse);

            // When
            List<ParticipationService.VolunteerRanking> result = 
                    participationService.getVolunteerRankings();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(5, result.get(0).getParticipationCount());
            assertEquals(1L, result.get(0).getVolunteer().getId());
        }
    }

    @Nested
    @DisplayName("Check Participation Tests")
    class CheckParticipationTests {

        @Test
        @DisplayName("Should return true when user is participating")
        void shouldReturnTrueWhenUserIsParticipating() {
            // Given
            when(taskService.getTaskEntityById(1L)).thenReturn(openTask);
            when(userService.getUserEntityByKeycloakSubject("volunteer-subject")).thenReturn(volunteer);
            when(participationRepository.existsByVolunteerAndTaskAndActiveTrue(volunteer, openTask))
                    .thenReturn(true);

            // When
            boolean result = participationService.isUserParticipating(1L, "volunteer-subject");

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false when user is not participating")
        void shouldReturnFalseWhenUserIsNotParticipating() {
            // Given
            when(taskService.getTaskEntityById(1L)).thenReturn(openTask);
            when(userService.getUserEntityByKeycloakSubject("volunteer-subject")).thenReturn(volunteer);
            when(participationRepository.existsByVolunteerAndTaskAndActiveTrue(volunteer, openTask))
                    .thenReturn(false);

            // When
            boolean result = participationService.isUserParticipating(1L, "volunteer-subject");

            // Then
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Task Status Specific Tests")
    class TaskStatusSpecificTests {

        @Test
        @DisplayName("Should allow joining IN_PROGRESS task")
        void shouldAllowJoiningInProgressTask() {
            // Given
            Task inProgressTask = createTask(4L, "In Progress Task", sensitiveUser, TaskStatus.IN_PROGRESS);
            when(taskService.getTaskEntityById(4L)).thenReturn(inProgressTask);
            when(userService.getUserEntityByKeycloakSubject("volunteer-subject")).thenReturn(volunteer);
            when(participationRepository.existsByVolunteerAndTaskAndActiveTrue(volunteer, inProgressTask))
                    .thenReturn(false);
            when(participationRepository.save(any(Participation.class))).thenReturn(participation);

            // When
            participationService.joinTask(4L, "volunteer-subject");

            // Then
            verify(participationRepository).save(any(Participation.class));
            // Should not update status since it's already IN_PROGRESS
            verify(taskService).updateTaskStatusToInProgress(4L);
        }
    }

    // Helper methods
    private User createUser(Long id, String email, UserType userType, String keycloakSubject, Boolean active) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setUserType(userType);
        user.setKeycloakSubject(keycloakSubject);
        user.setActive(active);
        user.setFirstName("Test");
        user.setLastName("User");
        return user;
    }

    private Task createTask(Long id, String title, User author, TaskStatus status) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setDescription("Test Description");
        task.setAuthor(author);
        task.setStatus(status);
        task.setCreatedAt(LocalDateTime.now());
        return task;
    }
}
