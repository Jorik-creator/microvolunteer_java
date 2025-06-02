package com.microvolunteer.service;

import com.microvolunteer.dto.request.UserRegistrationRequest;
import com.microvolunteer.dto.response.UserResponse;
import com.microvolunteer.entity.User;
import com.microvolunteer.enums.UserType;
import com.microvolunteer.exception.BusinessException;
import com.microvolunteer.mapper.UserMapper;
import com.microvolunteer.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserRegistrationRequest registrationRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        user = createUser(1L, "test@example.com", UserType.VOLUNTEER, "test-subject");
        
        registrationRequest = new UserRegistrationRequest();
        registrationRequest.setFirstName("John");
        registrationRequest.setLastName("Doe");
        registrationRequest.setEmail("test@example.com");
        registrationRequest.setUserType(UserType.VOLUNTEER);

        userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setEmail("test@example.com");
        userResponse.setFirstName("John");
        userResponse.setLastName("Doe");
        userResponse.setUserType(UserType.VOLUNTEER);
    }

    @Nested
    @DisplayName("User Registration Tests")
    class UserRegistrationTests {

        @Test
        @DisplayName("Should register user successfully")
        void shouldRegisterUserSuccessfully() {
            // Given
            when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
            when(userRepository.existsByKeycloakSubject("test-subject")).thenReturn(false);
            when(userMapper.toEntity(registrationRequest)).thenReturn(user);
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            // When
            UserResponse result = userService.registerUser(registrationRequest, "test-subject");

            // Then
            assertNotNull(result);
            assertEquals("test@example.com", result.getEmail());
            assertEquals("John", result.getFirstName());
            assertEquals("Doe", result.getLastName());
            assertEquals(UserType.VOLUNTEER, result.getUserType());

            verify(userRepository).existsByEmail("test@example.com");
            verify(userRepository).existsByKeycloakSubject("test-subject");
            verify(userRepository).save(any(User.class));
            verify(userMapper).toEntity(registrationRequest);
            verify(userMapper).toResponse(user);
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            // Given
            when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.registerUser(registrationRequest, "test-subject"));

            assertEquals("USER_ALREADY_EXISTS", exception.getCode());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when Keycloak subject already exists")
        void shouldThrowExceptionWhenKeycloakSubjectExists() {
            // Given
            when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
            when(userRepository.existsByKeycloakSubject("test-subject")).thenReturn(true);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.registerUser(registrationRequest, "test-subject"));

            assertEquals("USER_ALREADY_EXISTS", exception.getCode());
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Get User Tests")
    class GetUserTests {

        @Test
        @DisplayName("Should get user by Keycloak subject successfully")
        void shouldGetUserByKeycloakSubjectSuccessfully() {
            // Given
            when(userRepository.findByKeycloakSubject("test-subject")).thenReturn(Optional.of(user));
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            // When
            UserResponse result = userService.getUserByKeycloakSubject("test-subject");

            // Then
            assertNotNull(result);
            assertEquals("test@example.com", result.getEmail());
            verify(userRepository).findByKeycloakSubject("test-subject");
            verify(userMapper).toResponse(user);
        }

        @Test
        @DisplayName("Should throw exception when user not found by Keycloak subject")
        void shouldThrowExceptionWhenUserNotFoundByKeycloakSubject() {
            // Given
            when(userRepository.findByKeycloakSubject("test-subject")).thenReturn(Optional.empty());

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.getUserByKeycloakSubject("test-subject"));

            assertEquals("USER_NOT_FOUND", exception.getCode());
        }

        @Test
        @DisplayName("Should get user entity by Keycloak subject successfully")
        void shouldGetUserEntityByKeycloakSubjectSuccessfully() {
            // Given
            when(userRepository.findByKeycloakSubject("test-subject")).thenReturn(Optional.of(user));

            // When
            User result = userService.getUserEntityByKeycloakSubject("test-subject");

            // Then
            assertNotNull(result);
            assertEquals("test@example.com", result.getEmail());
            verify(userRepository).findByKeycloakSubject("test-subject");
        }

        @Test
        @DisplayName("Should get user by ID successfully")
        void shouldGetUserByIdSuccessfully() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            // When
            UserResponse result = userService.getUserById(1L);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getId());
            verify(userRepository).findById(1L);
            verify(userMapper).toResponse(user);
        }

        @Test
        @DisplayName("Should throw exception when user not found by ID")
        void shouldThrowExceptionWhenUserNotFoundById() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.getUserById(1L));

            assertEquals("USER_NOT_FOUND", exception.getCode());
        }

        @Test
        @DisplayName("Should get user entity by ID successfully")
        void shouldGetUserEntityByIdSuccessfully() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            // When
            User result = userService.getUserEntityById(1L);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getId());
            verify(userRepository).findById(1L);
        }
    }

    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user successfully")
        void shouldUpdateUserSuccessfully() {
            // Given
            UserRegistrationRequest updateRequest = new UserRegistrationRequest();
            updateRequest.setFirstName("Jane");
            updateRequest.setLastName("Smith");
            updateRequest.setEmail("test@example.com"); // Same email
            updateRequest.setUserType(UserType.VOLUNTEER);

            when(userRepository.findByKeycloakSubject("test-subject")).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            // When
            UserResponse result = userService.updateUser("test-subject", updateRequest);

            // Then
            assertNotNull(result);
            verify(userRepository).findByKeycloakSubject("test-subject");
            verify(userMapper).updateEntity(updateRequest, user);
            verify(userRepository).save(user);
            verify(userMapper).toResponse(user);
        }

        @Test
        @DisplayName("Should throw exception when updating to existing email")
        void shouldThrowExceptionWhenUpdatingToExistingEmail() {
            // Given
            UserRegistrationRequest updateRequest = new UserRegistrationRequest();
            updateRequest.setEmail("another@example.com"); // Different email

            when(userRepository.findByKeycloakSubject("test-subject")).thenReturn(Optional.of(user));
            when(userRepository.existsByEmail("another@example.com")).thenReturn(true);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.updateUser("test-subject", updateRequest));

            assertEquals("USER_ALREADY_EXISTS", exception.getCode());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should allow email update when email doesn't exist")
        void shouldAllowEmailUpdateWhenEmailDoesntExist() {
            // Given
            UserRegistrationRequest updateRequest = new UserRegistrationRequest();
            updateRequest.setEmail("new@example.com");
            updateRequest.setFirstName("Jane");
            updateRequest.setLastName("Smith");
            updateRequest.setUserType(UserType.VOLUNTEER);

            when(userRepository.findByKeycloakSubject("test-subject")).thenReturn(Optional.of(user));
            when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            // When
            UserResponse result = userService.updateUser("test-subject", updateRequest);

            // Then
            assertNotNull(result);
            verify(userRepository).existsByEmail("new@example.com");
            verify(userRepository).save(user);
        }
    }

    @Nested
    @DisplayName("Get Users by Type Tests")
    class GetUsersByTypeTests {

        @Test
        @DisplayName("Should get volunteers successfully")
        void shouldGetVolunteersSuccessfully() {
            // Given
            List<User> volunteers = List.of(user);
            when(userRepository.findActiveUsersByType(UserType.VOLUNTEER)).thenReturn(volunteers);
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            // When
            List<UserResponse> result = userService.getVolunteers();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(UserType.VOLUNTEER, result.get(0).getUserType());
            verify(userRepository).findActiveUsersByType(UserType.VOLUNTEER);
        }

        @Test
        @DisplayName("Should get users by type successfully")
        void shouldGetUsersByTypeSuccessfully() {
            // Given
            List<User> sensitiveUsers = List.of(user);
            when(userRepository.findActiveUsersByType(UserType.SENSITIVE)).thenReturn(sensitiveUsers);
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            // When
            List<UserResponse> result = userService.getUsersByType(UserType.SENSITIVE);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(userRepository).findActiveUsersByType(UserType.SENSITIVE);
        }
    }

    @Nested
    @DisplayName("User Management Tests")
    class UserManagementTests {

        @Test
        @DisplayName("Should deactivate user successfully")
        void shouldDeactivateUserSuccessfully() {
            // Given
            when(userRepository.findByKeycloakSubject("test-subject")).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);

            // When
            userService.deactivateUser("test-subject");

            // Then
            assertFalse(user.isActive());
            verify(userRepository).findByKeycloakSubject("test-subject");
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should check if user exists by Keycloak subject")
        void shouldCheckIfUserExistsByKeycloakSubject() {
            // Given
            when(userRepository.existsByKeycloakSubject("test-subject")).thenReturn(true);

            // When
            boolean result = userService.existsByKeycloakSubject("test-subject");

            // Then
            assertTrue(result);
            verify(userRepository).existsByKeycloakSubject("test-subject");
        }
    }

    @Nested
    @DisplayName("Access Validation Tests")
    class AccessValidationTests {

        @Test
        @DisplayName("Should allow access when user owns resource")
        void shouldAllowAccessWhenUserOwnsResource() {
            // Given
            when(userRepository.findByKeycloakSubject("test-subject")).thenReturn(Optional.of(user));

            // When & Then
            assertDoesNotThrow(() -> userService.validateUserAccess("test-subject", 1L, "update profile"));
            verify(userRepository).findByKeycloakSubject("test-subject");
        }

        @Test
        @DisplayName("Should allow access when user is admin")
        void shouldAllowAccessWhenUserIsAdmin() {
            // Given
            User adminUser = createUser(2L, "admin@example.com", UserType.ADMIN, "admin-subject");
            when(userRepository.findByKeycloakSubject("admin-subject")).thenReturn(Optional.of(adminUser));

            // When & Then
            assertDoesNotThrow(() -> userService.validateUserAccess("admin-subject", 1L, "update profile"));
            verify(userRepository).findByKeycloakSubject("admin-subject");
        }

        @Test
        @DisplayName("Should throw exception when user doesn't own resource and is not admin")
        void shouldThrowExceptionWhenUserDoesntOwnResourceAndNotAdmin() {
            // Given
            User anotherUser = createUser(2L, "other@example.com", UserType.VOLUNTEER, "other-subject");
            when(userRepository.findByKeycloakSubject("other-subject")).thenReturn(Optional.of(anotherUser));

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userService.validateUserAccess("other-subject", 1L, "update profile"));

            assertEquals("UNAUTHORIZED_ACCESS", exception.getCode());
        }
    }

    // Helper method
    private User createUser(Long id, String email, UserType userType, String keycloakSubject) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setUserType(userType);
        user.setKeycloakSubject(keycloakSubject);
        user.setActive(true);
        user.setFirstName("John");
        user.setLastName("Doe");
        return user;
    }
}
