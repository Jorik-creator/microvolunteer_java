package com.microvolunteer.integration;

import com.microvolunteer.entity.Category;
import com.microvolunteer.entity.Participation;
import com.microvolunteer.entity.Task;
import com.microvolunteer.entity.User;
import com.microvolunteer.enums.TaskStatus;
import com.microvolunteer.enums.UserType;
import com.microvolunteer.repository.CategoryRepository;
import com.microvolunteer.repository.ParticipationRepository;
import com.microvolunteer.repository.TaskRepository;
import com.microvolunteer.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class ParticipationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ParticipationRepository participationRepository;

    private User vulnerableUser;
    private User volunteerUser;
    private User volunteerUser2;
    private Category testCategory;
    private Task availableTask;

    @BeforeEach
    void setUp() {
        // Create test users
        vulnerableUser = new User();
        vulnerableUser.setKeycloakId("vulnerable-user-id");
        vulnerableUser.setEmail("vulnerable@test.com");
        vulnerableUser.setFirstName("Vulnerable");
        vulnerableUser.setLastName("User");
        vulnerableUser.setUserType(UserType.AFFECTED_PERSON);
        vulnerableUser.setIsActive(true);
        vulnerableUser.setCreatedAt(LocalDateTime.now());
        vulnerableUser.setUpdatedAt(LocalDateTime.now());
        vulnerableUser = userRepository.save(vulnerableUser);

        volunteerUser = new User();
        volunteerUser.setKeycloakId("volunteer-user-id");
        volunteerUser.setEmail("volunteer@test.com");
        volunteerUser.setFirstName("Volunteer");
        volunteerUser.setLastName("User");
        volunteerUser.setUserType(UserType.VOLUNTEER);
        volunteerUser.setIsActive(true);
        volunteerUser.setCreatedAt(LocalDateTime.now());
        volunteerUser.setUpdatedAt(LocalDateTime.now());
        volunteerUser = userRepository.save(volunteerUser);

        volunteerUser2 = new User();
        volunteerUser2.setKeycloakId("volunteer-user-2-id");
        volunteerUser2.setEmail("volunteer2@test.com");
        volunteerUser2.setFirstName("Volunteer2");
        volunteerUser2.setLastName("User");
        volunteerUser2.setUserType(UserType.VOLUNTEER);
        volunteerUser2.setIsActive(true);
        volunteerUser2.setCreatedAt(LocalDateTime.now());
        volunteerUser2.setUpdatedAt(LocalDateTime.now());
        volunteerUser2 = userRepository.save(volunteerUser2);

        // Create test category
        testCategory = new Category();
        testCategory.setName("Test Category");
        testCategory.setDescription("Category for testing");
        testCategory.setIsActive(true);
        testCategory.setCreatedAt(LocalDateTime.now());
        testCategory.setUpdatedAt(LocalDateTime.now());
        testCategory = categoryRepository.save(testCategory);

        // Create test task
        availableTask = new Task();
        availableTask.setTitle("Available Task");
        availableTask.setDescription("Task available for participation");
        availableTask.setLocation("Test Location");
        availableTask.setStatus(TaskStatus.OPEN);
        availableTask.setCreator(vulnerableUser);
        availableTask.setCategory(testCategory);
        availableTask.setMaxParticipants(2);
        availableTask.setScheduledAt(LocalDateTime.now().plusDays(1));
        availableTask.setCreatedAt(LocalDateTime.now());
        availableTask.setUpdatedAt(LocalDateTime.now());
        availableTask = taskRepository.save(availableTask);
    }

    @Test
    @WithMockUser(username = "volunteer-user-id", roles = "USER")
    void joinTask_PositiveScenario_VolunteerJoinsAvailableTask() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/participations/task/" + availableTask.getId() + "/join"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.task.id").value(availableTask.getId()))
                .andExpect(jsonPath("$.user.id").value(volunteerUser.getId()));

        // Verify participation was created in database
        List<Participation> participations = participationRepository.findByTaskId(availableTask.getId());
        assertEquals(1, participations.size());
        assertEquals(volunteerUser.getId(), participations.get(0).getUser().getId());
    }

    @Test
    @WithMockUser(username = "volunteer-user-id", roles = "USER")
    void joinTask_NegativeScenario_AlreadyParticipating_ShouldReturnBadRequest() throws Exception {
        // Given - user already participating
        Participation existingParticipation = new Participation();
        existingParticipation.setTask(availableTask);
        existingParticipation.setUser(volunteerUser);
        existingParticipation.setJoinedAt(LocalDateTime.now());
        participationRepository.save(existingParticipation);

        // When & Then
        mockMvc.perform(post("/api/participations/task/" + availableTask.getId() + "/join"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "volunteer-user-2-id", roles = "USER")
    void joinTask_NegativeScenario_TaskFull_ShouldReturnBadRequest() throws Exception {
        // Given - task is full (max 2 participants, add 2 participants)
        Participation participation1 = new Participation();
        participation1.setTask(availableTask);
        participation1.setUser(volunteerUser);
        participation1.setJoinedAt(LocalDateTime.now());
        participationRepository.save(participation1);

        User anotherVolunteer = new User();
        anotherVolunteer.setKeycloakId("another-volunteer-id");
        anotherVolunteer.setEmail("another@test.com");
        anotherVolunteer.setFirstName("Another");
        anotherVolunteer.setLastName("Volunteer");
        anotherVolunteer.setUserType(UserType.VOLUNTEER);
        anotherVolunteer.setIsActive(true);
        anotherVolunteer.setCreatedAt(LocalDateTime.now());
        anotherVolunteer.setUpdatedAt(LocalDateTime.now());
        anotherVolunteer = userRepository.save(anotherVolunteer);

        Participation participation2 = new Participation();
        participation2.setTask(availableTask);
        participation2.setUser(anotherVolunteer);
        participation2.setJoinedAt(LocalDateTime.now());
        participationRepository.save(participation2);

        // When & Then - third user tries to join
        mockMvc.perform(post("/api/participations/task/" + availableTask.getId() + "/join"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "volunteer-user-id", roles = "USER")
    void joinTask_NegativeScenario_TaskNotFound_ShouldReturnNotFound() throws Exception {
        // Given - non-existent task ID
        Long nonExistentTaskId = 999L;

        // When & Then
        mockMvc.perform(post("/api/participations/task/" + nonExistentTaskId + "/join"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "volunteer-user-id", roles = "USER")
    void leaveTask_PositiveScenario_VolunteerLeavesTask() throws Exception {
        // Given - user is participating in task
        Participation participation = new Participation();
        participation.setTask(availableTask);
        participation.setUser(volunteerUser);
        participation.setJoinedAt(LocalDateTime.now());
        participation = participationRepository.save(participation);

        // When & Then
        mockMvc.perform(delete("/api/participations/task/" + availableTask.getId() + "/leave"))
                .andExpect(status().isOk());

        // Verify participation was deleted from database
        Optional<Participation> deletedParticipation = participationRepository.findById(participation.getId());
        assertFalse(deletedParticipation.isPresent());
    }

    @Test
    @WithMockUser(username = "volunteer-user-id", roles = "USER")
    void leaveTask_NegativeScenario_NotParticipating_ShouldReturnNotFound() throws Exception {
        // Given - user is not participating in task

        // When & Then
        mockMvc.perform(delete("/api/participations/task/" + availableTask.getId() + "/leave"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserParticipations_PositiveScenario_ShouldReturnUserParticipations() throws Exception {
        // Given - user has participations
        Participation participation1 = new Participation();
        participation1.setTask(availableTask);
        participation1.setUser(volunteerUser);
        participation1.setJoinedAt(LocalDateTime.now());
        participation1.setNotes("First participation");
        participationRepository.save(participation1);

        // Create another task and participation
        Task anotherTask = new Task();
        anotherTask.setTitle("Another Task");
        anotherTask.setDescription("Another task description");
        anotherTask.setLocation("Another Location");
        anotherTask.setStatus(TaskStatus.OPEN);
        anotherTask.setCreator(vulnerableUser);
        anotherTask.setCategory(testCategory);
        anotherTask.setMaxParticipants(1);
        anotherTask.setScheduledAt(LocalDateTime.now().plusDays(2));
        anotherTask.setCreatedAt(LocalDateTime.now());
        anotherTask.setUpdatedAt(LocalDateTime.now());
        anotherTask = taskRepository.save(anotherTask);

        Participation participation2 = new Participation();
        participation2.setTask(anotherTask);
        participation2.setUser(volunteerUser);
        participation2.setJoinedAt(LocalDateTime.now());
        participation2.setNotes("Second participation");
        participationRepository.save(participation2);

        // When & Then
        mockMvc.perform(get("/api/participations/user/" + volunteerUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].notes").exists())
                .andExpect(jsonPath("$[1].notes").exists());
    }

    @Test
    void getTaskParticipations_PositiveScenario_ShouldReturnTaskParticipations() throws Exception {
        // Given - task has multiple participants
        Participation participation1 = new Participation();
        participation1.setTask(availableTask);
        participation1.setUser(volunteerUser);
        participation1.setJoinedAt(LocalDateTime.now());
        participationRepository.save(participation1);

        Participation participation2 = new Participation();
        participation2.setTask(availableTask);
        participation2.setUser(volunteerUser2);
        participation2.setJoinedAt(LocalDateTime.now());
        participationRepository.save(participation2);

        // When & Then
        mockMvc.perform(get("/api/participations/task/" + availableTask.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].task.id").value(availableTask.getId()))
                .andExpect(jsonPath("$[1].task.id").value(availableTask.getId()));
    }

    @Test
    void getParticipationById_PositiveScenario_ShouldReturnParticipation() throws Exception {
        // Given - participation exists
        Participation participation = new Participation();
        participation.setTask(availableTask);
        participation.setUser(volunteerUser);
        participation.setJoinedAt(LocalDateTime.now());
        participation.setNotes("Test participation notes");
        participation = participationRepository.save(participation);

        // When & Then
        mockMvc.perform(get("/api/participations/" + participation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(participation.getId()))
                .andExpect(jsonPath("$.notes").value("Test participation notes"));
    }

    @Test
    void getParticipationById_NegativeScenario_ParticipationNotFound_ShouldReturnNotFound() throws Exception {
        // Given - non-existent participation ID
        Long nonExistentId = 999L;

        // When & Then
        mockMvc.perform(get("/api/participations/" + nonExistentId))
                .andExpect(status().isNotFound());
    }
}
