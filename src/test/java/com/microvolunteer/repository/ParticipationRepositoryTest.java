package com.microvolunteer.repository;

import com.microvolunteer.entity.Category;
import com.microvolunteer.entity.Participation;
import com.microvolunteer.entity.Task;
import com.microvolunteer.entity.User;
import com.microvolunteer.enums.TaskStatus;
import com.microvolunteer.enums.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ParticipationRepositoryTest {

    @Autowired
    private ParticipationRepository participationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User testUser;
    private User anotherUser;
    private Task testTask;
    private Task anotherTask;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        // Create test category
        testCategory = new Category();
        testCategory.setName("Test Category");
        testCategory.setDescription("Test description");
        testCategory.setIsActive(true);
        testCategory.setCreatedAt(LocalDateTime.now());
        testCategory.setUpdatedAt(LocalDateTime.now());
        testCategory = categoryRepository.save(testCategory);

        // Create test users
        testUser = new User();
        testUser.setKeycloakId("test-user-keycloak-id");
        testUser.setEmail("testuser@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setUserType(UserType.VOLUNTEER);
        testUser.setIsActive(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);

        anotherUser = new User();
        anotherUser.setKeycloakId("another-user-keycloak-id");
        anotherUser.setEmail("anotheruser@example.com");
        anotherUser.setFirstName("Another");
        anotherUser.setLastName("User");
        anotherUser.setUserType(UserType.VOLUNTEER);
        anotherUser.setIsActive(true);
        anotherUser.setCreatedAt(LocalDateTime.now());
        anotherUser.setUpdatedAt(LocalDateTime.now());
        anotherUser = userRepository.save(anotherUser);

        // Create test tasks
        testTask = new Task();
        testTask.setTitle("Test Task");
        testTask.setDescription("Test task description");
        testTask.setLocation("Test location");
        testTask.setStatus(TaskStatus.OPEN);
        testTask.setCreator(anotherUser);
        testTask.setCategory(testCategory);
        testTask.setMaxParticipants(3);
        testTask.setScheduledAt(LocalDateTime.now().plusDays(1));
        testTask.setCreatedAt(LocalDateTime.now());
        testTask.setUpdatedAt(LocalDateTime.now());
        testTask = taskRepository.save(testTask);

        anotherTask = new Task();
        anotherTask.setTitle("Another Task");
        anotherTask.setDescription("Another task description");
        anotherTask.setLocation("Another location");
        anotherTask.setStatus(TaskStatus.OPEN);
        anotherTask.setCreator(anotherUser);
        anotherTask.setCategory(testCategory);
        anotherTask.setMaxParticipants(2);
        anotherTask.setScheduledAt(LocalDateTime.now().plusDays(2));
        anotherTask.setCreatedAt(LocalDateTime.now());
        anotherTask.setUpdatedAt(LocalDateTime.now());
        anotherTask = taskRepository.save(anotherTask);
    }

    @Test
    void findByUserId_ShouldReturnUserParticipations() {
        // Given
        Participation participation1 = createParticipation(testTask, testUser, "First participation");
        Participation participation2 = createParticipation(anotherTask, testUser, "Second participation");
        participationRepository.save(participation1);
        participationRepository.save(participation2);

        // When
        List<Participation> userParticipations = participationRepository.findByUserId(testUser.getId());

        // Then
        assertEquals(2, userParticipations.size());
        assertTrue(userParticipations.stream()
                .allMatch(p -> p.getUser().getId().equals(testUser.getId())));
    }

    @Test
    void findByTaskId_ShouldReturnTaskParticipations() {
        // Given
        Participation participation1 = createParticipation(testTask, testUser, "User 1 participation");
        Participation participation2 = createParticipation(testTask, anotherUser, "User 2 participation");
        participationRepository.save(participation1);
        participationRepository.save(participation2);

        // When
        List<Participation> taskParticipations = participationRepository.findByTaskId(testTask.getId());

        // Then
        assertEquals(2, taskParticipations.size());
        assertTrue(taskParticipations.stream()
                .allMatch(p -> p.getTask().getId().equals(testTask.getId())));
    }

    @Test
    void findByTaskIdAndUserId_ExistingParticipation_ShouldReturnParticipation() {
        // Given
        Participation participation = createParticipation(testTask, testUser, "Test participation");
        participationRepository.save(participation);

        // When
        Optional<Participation> found = participationRepository.findByTaskIdAndUserId(
                testTask.getId(), testUser.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(testTask.getId(), found.get().getTask().getId());
        assertEquals(testUser.getId(), found.get().getUser().getId());
    }

    @Test
    void findByTaskIdAndUserId_NonExistingParticipation_ShouldReturnEmpty() {
        // When
        Optional<Participation> found = participationRepository.findByTaskIdAndUserId(
                testTask.getId(), testUser.getId());

        // Then
        assertTrue(found.isEmpty());
    }

    @Test
    void existsByTaskIdAndUserId_ExistingParticipation_ShouldReturnTrue() {
        // Given
        Participation participation = createParticipation(testTask, testUser, "Test participation");
        participationRepository.save(participation);

        // When
        boolean exists = participationRepository.existsByTaskIdAndUserId(
                testTask.getId(), testUser.getId());

        // Then
        assertTrue(exists);
    }

    @Test
    void existsByTaskIdAndUserId_NonExistingParticipation_ShouldReturnFalse() {
        // When
        boolean exists = participationRepository.existsByTaskIdAndUserId(
                testTask.getId(), testUser.getId());

        // Then
        assertFalse(exists);
    }

    @Test
    void countByTaskId_ShouldReturnCorrectCount() {
        // Given
        Participation participation1 = createParticipation(testTask, testUser, "Participation 1");
        Participation participation2 = createParticipation(testTask, anotherUser, "Participation 2");
        participationRepository.save(participation1);
        participationRepository.save(participation2);

        // When
        long count = participationRepository.countByTaskId(testTask.getId());

        // Then
        assertEquals(2, count);
    }

    @Test
    void countByTaskId_NoParticipations_ShouldReturnZero() {
        // When
        long count = participationRepository.countByTaskId(testTask.getId());

        // Then
        assertEquals(0, count);
    }

    @Test
    void countByUserId_ShouldReturnCorrectCount() {
        // Given
        Participation participation1 = createParticipation(testTask, testUser, "Participation 1");
        Participation participation2 = createParticipation(anotherTask, testUser, "Participation 2");
        participationRepository.save(participation1);
        participationRepository.save(participation2);

        // When
        long count = participationRepository.countByUserId(testUser.getId());

        // Then
        assertEquals(2, count);
    }

    @Test
    void save_ShouldPersistParticipation() {
        // Given
        Participation participation = createParticipation(testTask, testUser, "New participation");

        // When
        Participation savedParticipation = participationRepository.save(participation);

        // Then
        assertNotNull(savedParticipation.getId());
        assertEquals(testTask.getId(), savedParticipation.getTask().getId());
        assertEquals(testUser.getId(), savedParticipation.getUser().getId());
        assertEquals("New participation", savedParticipation.getNotes());
    }

    @Test
    void delete_ShouldRemoveParticipation() {
        // Given
        Participation participation = createParticipation(testTask, testUser, "Participation to delete");
        participation = participationRepository.save(participation);
        Long participationId = participation.getId();

        // When
        participationRepository.delete(participation);

        // Then
        Optional<Participation> deleted = participationRepository.findById(participationId);
        assertTrue(deleted.isEmpty());
    }

    @Test
    void findByUserIdOrderByJoinedAtDesc_ShouldReturnParticipationsInDescendingOrder() {
        // Given
        Participation oldParticipation = createParticipation(testTask, testUser, "Old participation");
        oldParticipation.setJoinedAt(LocalDateTime.now().minusDays(2));
        participationRepository.save(oldParticipation);

        Participation newParticipation = createParticipation(anotherTask, testUser, "New participation");
        newParticipation.setJoinedAt(LocalDateTime.now());
        participationRepository.save(newParticipation);

        // When
        List<Participation> participations = participationRepository.findByUserIdOrderByJoinedAtDesc(testUser.getId());

        // Then
        assertEquals(2, participations.size());
        assertTrue(participations.get(0).getJoinedAt().isAfter(participations.get(1).getJoinedAt()));
        assertEquals("New participation", participations.get(0).getNotes());
        assertEquals("Old participation", participations.get(1).getNotes());
    }

    private Participation createParticipation(Task task, User user, String notes) {
        Participation participation = new Participation();
        participation.setTask(task);
        participation.setUser(user);
        participation.setJoinedAt(LocalDateTime.now());
        participation.setNotes(notes);
        return participation;
    }
}
