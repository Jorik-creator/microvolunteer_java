package com.microvolunteer.integration;

import com.microvolunteer.dto.request.UserRegistrationRequest;
import com.microvolunteer.enums.UserType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("User Integration Tests")
class UserIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Complete user registration and profile management workflow")
    void shouldCompleteUserWorkflowSuccessfully() {
        // 1. Register new user
        UserRegistrationRequest registrationRequest = createRegistrationRequest();
        
        Long userId = asSensitiveUser()
                .body(registrationRequest)
                .when()
                .post("/api/users/register")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("firstName", equalTo("John"))
                .body("lastName", equalTo("Doe"))
                .body("email", equalTo("john.doe@integration.test"))
                .body("userType", equalTo("SENSITIVE"))
                .body("active", equalTo(true))
                .extract()
                .path("id");

        // 2. Get user profile
        asSensitiveUser()
                .when()
                .get("/api/users/profile")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(userId.intValue()))
                .body("firstName", equalTo("John"))
                .body("lastName", equalTo("Doe"))
                .body("email", equalTo("john.doe@integration.test"));

        // 3. Update user profile
        UserRegistrationRequest updateRequest = new UserRegistrationRequest();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Smith");
        updateRequest.setEmail("jane.smith@integration.test");
        updateRequest.setUserType(UserType.SENSITIVE);
        updateRequest.setPhone("+9876543210");

        asSensitiveUser()
                .body(updateRequest)
                .when()
                .put("/api/users/profile")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("firstName", equalTo("Jane"))
                .body("lastName", equalTo("Smith"))
                .body("email", equalTo("jane.smith@integration.test"))
                .body("phone", equalTo("+9876543210"));

        // 4. Get user by ID
        given()
                .when()
                .get("/api/users/{id}", userId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(userId.intValue()))
                .body("firstName", equalTo("Jane"))
                .body("lastName", equalTo("Smith"));
    }

    @Test
    @DisplayName("Should prevent duplicate user registration")
    void shouldPreventDuplicateUserRegistration() {
        // Register first user
        UserRegistrationRequest request = createRegistrationRequest();
        request.setEmail("duplicate@integration.test");

        asSensitiveUser()
                .body(request)
                .when()
                .post("/api/users/register")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        // Try to register user with same email
        UserRegistrationRequest duplicateRequest = createRegistrationRequest();
        duplicateRequest.setEmail("duplicate@integration.test");
        duplicateRequest.setFirstName("Another");
        duplicateRequest.setLastName("User");

        asVolunteerUser()
                .body(duplicateRequest)
                .when()
                .post("/api/users/register")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body("message", containsString("already exists"));
    }

    @Test
    @DisplayName("Should get volunteers list")
    void shouldGetVolunteersList() {
        // Register volunteer users
        registerVolunteerUser("volunteer1@integration.test", "Vol1", "User1");
        registerVolunteerUser("volunteer2@integration.test", "Vol2", "User2");

        // Get volunteers list
        given()
                .when()
                .get("/api/users/volunteers")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(greaterThanOrEqualTo(2)))
                .body("findAll { it.userType == 'VOLUNTEER' }", hasSize(greaterThanOrEqualTo(2)));
    }

    @Test
    @DisplayName("Should handle profile update validation")
    void shouldHandleProfileUpdateValidation() {
        // Register user first
        UserRegistrationRequest registrationRequest = createRegistrationRequest();
        registrationRequest.setEmail("updatetest@integration.test");

        asSensitiveUser()
                .body(registrationRequest)
                .when()
                .post("/api/users/register")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        // Try to update with invalid data
        UserRegistrationRequest invalidUpdate = new UserRegistrationRequest();
        invalidUpdate.setFirstName(""); // Empty first name
        invalidUpdate.setLastName("Smith");
        invalidUpdate.setEmail("invalid-email"); // Invalid email format
        invalidUpdate.setUserType(UserType.SENSITIVE);

        asSensitiveUser()
                .body(invalidUpdate)
                .when()
                .put("/api/users/profile")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Should handle user deactivation")
    void shouldHandleUserDeactivation() {
        // Register user
        UserRegistrationRequest request = createRegistrationRequest();
        request.setEmail("deactivate@integration.test");

        asSensitiveUser()
                .body(request)
                .when()
                .post("/api/users/register")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        // Verify user is active
        asSensitiveUser()
                .when()
                .get("/api/users/profile")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("active", equalTo(true));

        // Deactivate user
        asSensitiveUser()
                .when()
                .delete("/api/users/profile")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        // Note: After deactivation, user profile might still be accessible
        // depending on business logic, but marked as inactive
    }

    @Test
    @DisplayName("Should handle email update conflicts")
    void shouldHandleEmailUpdateConflicts() {
        // Register first user
        UserRegistrationRequest firstUser = createRegistrationRequest();
        firstUser.setEmail("first@integration.test");

        asSensitiveUser()
                .body(firstUser)
                .when()
                .post("/api/users/register")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        // Register second user
        UserRegistrationRequest secondUser = createRegistrationRequest();
        secondUser.setEmail("second@integration.test");
        secondUser.setFirstName("Second");
        secondUser.setLastName("User");

        asVolunteerUser()
                .body(secondUser)
                .when()
                .post("/api/users/register")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        // Try to update second user's email to first user's email
        UserRegistrationRequest conflictUpdate = new UserRegistrationRequest();
        conflictUpdate.setFirstName("Second");
        conflictUpdate.setLastName("User");
        conflictUpdate.setEmail("first@integration.test"); // Conflicting email
        conflictUpdate.setUserType(UserType.VOLUNTEER);

        asVolunteerUser()
                .body(conflictUpdate)
                .when()
                .put("/api/users/profile")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body("message", containsString("already exists"));
    }

    @Test
    @DisplayName("Should handle not found scenarios")
    void shouldHandleNotFoundScenarios() {
        // Try to get non-existent user by ID
        given()
                .when()
                .get("/api/users/{id}", 99999L)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("message", containsString("not found"));
    }

    @Test
    @DisplayName("Should require authentication for protected endpoints")
    void shouldRequireAuthenticationForProtectedEndpoints() {
        // Profile endpoints should require authentication
        given()
                .when()
                .get("/api/users/profile")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());

        given()
                .body(createRegistrationRequest())
                .when()
                .post("/api/users/register")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());

        given()
                .body(createRegistrationRequest())
                .when()
                .put("/api/users/profile")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());

        given()
                .when()
                .delete("/api/users/profile")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @DisplayName("Should handle user type specific behaviors")
    void shouldHandleUserTypeSpecificBehaviors() {
        // Register SENSITIVE user
        UserRegistrationRequest sensitiveRequest = createRegistrationRequest();
        sensitiveRequest.setEmail("sensitive.specific@integration.test");
        sensitiveRequest.setUserType(UserType.SENSITIVE);

        asSensitiveUser()
                .body(sensitiveRequest)
                .when()
                .post("/api/users/register")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("userType", equalTo("SENSITIVE"));

        // Register VOLUNTEER user
        UserRegistrationRequest volunteerRequest = createRegistrationRequest();
        volunteerRequest.setEmail("volunteer.specific@integration.test");
        volunteerRequest.setUserType(UserType.VOLUNTEER);
        volunteerRequest.setFirstName("Volunteer");

        asVolunteerUser()
                .body(volunteerRequest)
                .when()
                .post("/api/users/register")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("userType", equalTo("VOLUNTEER"));

        // Verify volunteers endpoint includes the volunteer
        given()
                .when()
                .get("/api/users/volunteers")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("find { it.email == 'volunteer.specific@integration.test' }", notNullValue())
                .body("find { it.email == 'volunteer.specific@integration.test' }.firstName", equalTo("Volunteer"));
    }

    // Helper methods
    private UserRegistrationRequest createRegistrationRequest() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@integration.test");
        request.setUserType(UserType.SENSITIVE);
        request.setPhone("+1234567890");
        return request;
    }

    private void registerVolunteerUser(String email, String firstName, String lastName) {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setEmail(email);
        request.setUserType(UserType.VOLUNTEER);
        request.setPhone("+1234567890");

        asVolunteerUser()
                .body(request)
                .when()
                .post("/api/users/register")
                .then()
                .statusCode(HttpStatus.CREATED.value());
    }
}
