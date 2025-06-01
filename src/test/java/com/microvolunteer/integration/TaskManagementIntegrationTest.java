package com.microvolunteer.integration;

import com.microvolunteer.dto.request.TaskCreateRequest;
import com.microvolunteer.entity.Category;
import com.microvolunteer.entity.Task;
import com.microvolunteer.entity.User;
import com.microvolunteer.enums.TaskStatus;
import com.microvolunteer.enums.UserType;
import com.microvolunteer.repository.CategoryRepository;
import com.microvolunteer.repository.TaskRepository;
import com.microvolunteer.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class TaskManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User vulnerableUser;
    private User volunteerUser;
    private Category testCategory;

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

        // Create test category
        testCategory = new Category();
        testCategory.setName("Test Category");
        testCategory.setDescription("Category for testing");
        testCategory.setIsActive(true);
        testCategory.setCreatedAt(LocalDateTime.now());
        testCategory.setUpdatedAt(LocalDateTime.now());
        testCategory = categoryRepository.save(testCategory);
    }

    @Test
    @WithMockUser(username = "vulnerable-user-id", roles = "USER")
    void createTask_PositiveScenario_VulnerableUserCreatesTask() throws Exception {
        // Given
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("Допоможіть з покупками");
        request.setDescription("Потрібна допомога з покупкою продуктів");
        request.setLocation("АТБ, вул. Хрещатик, 20");
        request.setCategoryId(testCategory.getId());
        request.setMaxParticipants(2);
        request.setScheduledAt(LocalDateTime.now().plusDays(1));

        // When & Then
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Допоможіть з покупками"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.maxParticipants").value(2));

        // Verify task was created in database
        java.util.List<Task> createdTasks = taskRepository.findByTitle("Допоможіть з покупками");
        assertTrue(!createdTasks.isEmpty());
        assertEquals(vulnerableUser.getId(), createdTasks.get(0).getCreator().getId());
        assertEquals(TaskStatus.OPEN, createdTasks.get(0).getStatus());
    }

    @Test
    @WithMockUser(username = "vulnerable-user-id", roles = "USER")
    void createTask_NegativeScenario_InvalidData_ShouldReturnBadRequest() throws Exception {
        // Given - request with missing required fields
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle(""); // Empty title
        request.setDescription("Valid description");
        request.setCategoryId(999L); // Non-existent category

        // When & Then
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "volunteer-user-id", roles = "USER")
    void searchTasks_PositiveScenario_VolunteerFindsAvailableTasks() throws Exception {
        // Given - create some test tasks
        Task task1 = new Task();
        task1.setTitle("Допомога з покупками");
        task1.setDescription("Потрібна допомога з покупкою продуктів");
        task1.setLocation("Магазин");
        task1.setStatus(TaskStatus.OPEN);
        task1.setCreator(vulnerableUser);
        task1.setCategory(testCategory);
        task1.setMaxParticipants(2);
        task1.setScheduledAt(LocalDateTime.now().plusDays(1));
        task1.setCreatedAt(LocalDateTime.now());
        task1.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task1);

        Task task2 = new Task();
        task2.setTitle("Супровід до лікаря");
        task2.setDescription("Потрібен супровід до поліклініки");
        task2.setLocation("Поліклініка");
        task2.setStatus(TaskStatus.OPEN);
        task2.setCreator(vulnerableUser);
        task2.setCategory(testCategory);
        task2.setMaxParticipants(1);
        task2.setScheduledAt(LocalDateTime.now().plusDays(2));
        task2.setCreatedAt(LocalDateTime.now());
        task2.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task2);

        // When & Then - search all tasks
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").exists())
                .andExpect(jsonPath("$[1].title").exists());
    }

    @Test
    @WithMockUser(username = "volunteer-user-id", roles = "USER")
    void searchTasks_PositiveScenario_FilterByCategory() throws Exception {
        // Given - create tasks in different categories
        Category category2 = new Category();
        category2.setName("Different Category");
        category2.setDescription("Another category");
        category2.setIsActive(true);
        category2.setCreatedAt(LocalDateTime.now());
        category2.setUpdatedAt(LocalDateTime.now());
        category2 = categoryRepository.save(category2);

        Task task1 = new Task();
        task1.setTitle("Task in test category");
        task1.setDescription("Description");
        task1.setLocation("Location");
        task1.setStatus(TaskStatus.OPEN);
        task1.setCreator(vulnerableUser);
        task1.setCategory(testCategory);
        task1.setMaxParticipants(1);
        task1.setScheduledAt(LocalDateTime.now().plusDays(1));
        task1.setCreatedAt(LocalDateTime.now());
        task1.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task1);

        Task task2 = new Task();
        task2.setTitle("Task in different category");
        task2.setDescription("Description");
        task2.setLocation("Location");
        task2.setStatus(TaskStatus.OPEN);
        task2.setCreator(vulnerableUser);
        task2.setCategory(category2);
        task2.setMaxParticipants(1);
        task2.setScheduledAt(LocalDateTime.now().plusDays(1));
        task2.setCreatedAt(LocalDateTime.now());
        task2.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task2);

        // When & Then - search by specific category
        mockMvc.perform(get("/api/tasks/category/" + testCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Task in test category"));
    }

    @Test
    @WithMockUser(username = "volunteer-user-id", roles = "USER")
    void searchTasks_NegativeScenario_NoTasksFound() throws Exception {
        // Given - no tasks in database

        // When & Then
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getTaskById_PositiveScenario_TaskExists() throws Exception {
        // Given
        Task task = new Task();
        task.setTitle("Existing Task");
        task.setDescription("Task description");
        task.setLocation("Location");
        task.setStatus(TaskStatus.OPEN);
        task.setCreator(vulnerableUser);
        task.setCategory(testCategory);
        task.setMaxParticipants(1);
        task.setScheduledAt(LocalDateTime.now().plusDays(1));
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task = taskRepository.save(task);

        // When & Then
        mockMvc.perform(get("/api/tasks/" + task.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Existing Task"))
                .andExpect(jsonPath("$.id").value(task.getId()));
    }

    @Test
    void getTaskById_NegativeScenario_TaskNotFound() throws Exception {
        // Given - non-existent task ID
        Long nonExistentId = 999L;

        // When & Then
        mockMvc.perform(get("/api/tasks/" + nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "vulnerable-user-id", roles = "USER")
    void completeTask_PositiveScenario_TaskOwnerCompletesTask() throws Exception {
        // Given
        Task task = new Task();
        task.setTitle("Task to Complete");
        task.setDescription("Task description");
        task.setLocation("Location");
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setCreator(vulnerableUser);
        task.setCategory(testCategory);
        task.setMaxParticipants(1);
        task.setScheduledAt(LocalDateTime.now().plusDays(1));
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task = taskRepository.save(task);

        // When & Then
        mockMvc.perform(put("/api/tasks/" + task.getId() + "/complete")
                .param("userId", vulnerableUser.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // Verify task status updated
        Optional<Task> updatedTask = taskRepository.findById(task.getId());
        assertTrue(updatedTask.isPresent());
        assertEquals(TaskStatus.COMPLETED, updatedTask.get().getStatus());
    }

    @Test
    @WithMockUser(username = "volunteer-user-id", roles = "USER")
    void completeTask_NegativeScenario_NotTaskOwner_ShouldReturnNotFound() throws Exception {
        // Given
        Task task = new Task();
        task.setTitle("Task owned by someone else");
        task.setDescription("Task description");
        task.setLocation("Location");
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setCreator(vulnerableUser); // Owned by vulnerable user
        task.setCategory(testCategory);
        task.setMaxParticipants(1);
        task.setScheduledAt(LocalDateTime.now().plusDays(1));
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task = taskRepository.save(task);

        // When & Then - volunteer tries to complete task they don't own
        mockMvc.perform(put("/api/tasks/" + task.getId() + "/complete")
                .param("userId", volunteerUser.getId().toString()))
                .andExpect(status().isNotFound());
    }
}
