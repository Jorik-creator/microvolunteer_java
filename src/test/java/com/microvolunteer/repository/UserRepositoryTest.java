package com.microvolunteer.repository;

import com.microvolunteer.entity.User;
import com.microvolunteer.enums.UserType;
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
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmail_ExistingUser_ShouldReturnUser() {
        // Given
        User user = createTestUser("test@example.com", "test-keycloak-id");
        userRepository.save(user);

        // When
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Then
        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void findByEmail_NonExistingUser_ShouldReturnEmpty() {
        // When
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertTrue(found.isEmpty());
    }

    @Test
    void findByKeycloakId_ExistingUser_ShouldReturnUser() {
        // Given
        User user = createTestUser("test@example.com", "test-keycloak-id");
        userRepository.save(user);

        // When
        Optional<User> found = userRepository.findByKeycloakId("test-keycloak-id");

        // Then
        assertTrue(found.isPresent());
        assertEquals("test-keycloak-id", found.get().getKeycloakId());
    }

    @Test
    void findByKeycloakId_NonExistingUser_ShouldReturnEmpty() {
        // When
        Optional<User> found = userRepository.findByKeycloakId("nonexistent-keycloak-id");

        // Then
        assertTrue(found.isEmpty());
    }

    @Test
    void existsByEmail_ExistingUser_ShouldReturnTrue() {
        // Given
        User user = createTestUser("test@example.com", "test-keycloak-id");
        userRepository.save(user);

        // When
        boolean exists = userRepository.existsByEmail("test@example.com");

        // Then
        assertTrue(exists);
    }

    @Test
    void existsByEmail_NonExistingUser_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertFalse(exists);
    }



    @Test
    void findByIsActiveTrue_ShouldReturnOnlyActiveUsers() {
        // Given
        User activeUser = createTestUser("active@example.com", "active-keycloak-id");
        activeUser.setIsActive(true);
        userRepository.save(activeUser);

        User inactiveUser = createTestUser("inactive@example.com", "inactive-keycloak-id");
        inactiveUser.setIsActive(false);
        userRepository.save(inactiveUser);

        // When
        List<User> activeUsers = userRepository.findByIsActiveTrue();

        // Then
        assertEquals(1, activeUsers.size());
        assertTrue(activeUsers.get(0).getIsActive());
        assertEquals("active@example.com", activeUsers.get(0).getEmail());
    }

    @Test
    void save_ShouldPersistUser() {
        // Given
        User user = createTestUser("save@example.com", "save-keycloak-id");

        // When
        User savedUser = userRepository.save(user);

        // Then
        assertNotNull(savedUser.getId());
        assertEquals("save@example.com", savedUser.getEmail());
        assertEquals("save-keycloak-id", savedUser.getKeycloakId());
    }

    @Test
    void deleteById_ShouldRemoveUser() {
        // Given
        User user = createTestUser("delete@example.com", "delete-keycloak-id");
        user = userRepository.save(user);
        Long userId = user.getId();

        // When
        userRepository.deleteById(userId);

        // Then
        Optional<User> deleted = userRepository.findById(userId);
        assertTrue(deleted.isEmpty());
    }

    private User createTestUser(String email, String keycloakId) {
        User user = new User();
        user.setEmail(email);
        user.setKeycloakId(keycloakId);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setUserType(UserType.VOLUNTEER);
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}
