package com.microvolunteer.repository;

import com.microvolunteer.entity.Category;
import com.microvolunteer.entity.Task;
import com.microvolunteer.entity.User;
import com.microvolunteer.enums.TaskStatus;
import com.microvolunteer.enums.UserType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void findByStatus_ShouldReturnTasksWithSpecificStatus() {
        // Given
        User creator = createTestUser();
        Category category = createTestCategory();
        Task openTask = createTestTask("Open Task", TaskStatus.OPEN, creator, category);
        Task completedTask = createTestTask("Completed Task", TaskStatus.COMPLETED, creator, category);
        
        entityManager.persistAndFlush(creator);
        entityManager.persistAndFlush(category);
        entityManager.persistAndFlush(openTask);
        entityManager.persistAndFlush(completedTask);

        // When
        Page<Task> result = taskRepository.findByStatus(TaskStatus.OPEN, PageRequest.of(0, 10));

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals("Open Task", result.getContent().get(0).getTitle());
        assertEquals(TaskStatus.OPEN, result.getContent().get(0).getStatus());
    }

    @Test
    void findByCategoryId_ShouldReturnTasksFromSpecificCategory() {
        // Given
        User creator = createTestUser();
        Category category1 = createTestCategory("Category 1");
        Category category2 = createTestCategory("Category 2");
        Task task1 = createTestTask("Task 1", TaskStatus.OPEN, creator, category1);
        Task task2 = createTestTask("Task 2", TaskStatus.OPEN, creator, category2);
        
        entityManager.persistAndFlush(creator);
        entityManager.persistAndFlush(category1);
        entityManager.persistAndFlush(category2);
        entityManager.persistAndFlush(task1);
        entityManager.persistAndFlush(task2);

        // When
        List<Task> result = taskRepository.findByCategoryId(category1.getId());

        // Then
        assertEquals(1, result.size());
        assertEquals("Task 1", result.get(0).getTitle());
        assertEquals(category1.getId(), result.get(0).getCategory().getId());
    }

    @Test
    void findByCreatorId_ShouldReturnTasksFromSpecificCreator() {
        // Given
        User creator1 = createTestUser("user1@example.com");
        User creator2 = createTestUser("user2@example.com");
        Category category = createTestCategory();
        Task task1 = createTestTask("Task 1", TaskStatus.OPEN, creator1, category);
        Task task2 = createTestTask("Task 2", TaskStatus.OPEN, creator2, category);
        
        entityManager.persistAndFlush(creator1);
        entityManager.persistAndFlush(creator2);
        entityManager.persistAndFlush(category);
        entityManager.persistAndFlush(task1);
        entityManager.persistAndFlush(task2);

        // When
        List<Task> result = taskRepository.findByCreatorId(creator1.getId());

        // Then
        assertEquals(1, result.size());
        assertEquals("Task 1", result.get(0).getTitle());
        assertEquals(creator1.getId(), result.get(0).getCreator().getId());
    }

    private User createTestUser() {
        return createTestUser("test@example.com");
    }

    private User createTestUser(String email) {
        return User.builder()
                .keycloakId("test-keycloak-id-" + email)
                .email(email)
                .firstName("John")  // Виправлено з firstN
                .lastName("Doe")    // Виправлено з lastN
                .userType(UserType.VOLUNTEER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private Category createTestCategory() {
        return createTestCategory("Test Category");
    }

    private Category createTestCategory(String name) {
        return Category.builder()
                .name(name)
                .description("Test Description")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private Task createTestTask(String title, TaskStatus status, User creator, Category category) {
        return Task.builder()
                .title(title)
                .description("Test Description")
                .status(status)
                .creator(creator)
                .category(category)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
