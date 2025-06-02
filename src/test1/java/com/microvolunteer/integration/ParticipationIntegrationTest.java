package com.microvolunteer.integration;

import com.microvolunteer.dto.request.TaskCreateRequest;
import com.microvolunteer.dto.request.UserRegistrationRequest;
import com.microvolunteer.enums.UserType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Participation Integration Tests")
class ParticipationIntegrationTest extends BaseIntegrationTest {

    private Long sensitiveUserId;
    private Long volunteer1Id;
    private Long volunteer2Id;
    private Long taskId;

    @BeforeEach
    void setUp() {
        // Create test users
        sensitiveUserId = createSensitiveUser();
        volunteer1Id = createVolunteerUser("volunteer1@integration.test", "Vol1", "User1");
        volunteer2Id = createVolunteerUser("volunteer2@integration.test", "Vol2", "User2");
        
        // Create a test task
        taskId = createTestTask();
    }

    @Test
    @DisplayName("Complete participation workflow: join, check status, get volunteers, leave")
    void shouldCompleteParticipationWorkflowSuccessfully() {
        // 1. Check initial participation status (should be false)
        asVolunteerUser()
                .when()
                .get("/api/tasks/{id}/participation-status", taskId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("false"));

        // 2. Check initial volunteers list (should be empty)
        given()
                .when()
                .get("/api/tasks/{id}/volunteers", taskId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(0));

        // 3. First volunteer joins the task
        asVolunteerUser()
                .when()
                .post("/api/tasks/{id}/participate", taskId)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        // 4. Check participation status after joining (should be true)
        asVolunteerUser()
                .when()
                .get("/api/tasks/{id}/participation-status", taskId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("true"));

        // 5. Check volunteers list (should contain 1 volunteer)
        given()
                .when()
                .get("/api/tasks/{id}/volunteers", taskId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(1))
                .body("[0].userType", equalTo("VOLUNTEER"))
                .body("[0].email", equalTo("volunteer@integration.test"));

        // 6. Second volunteer joins the task
        authenticatedRequest(createMockJwtToken("volunteer2-user", "VOLUNTEER"))
                .when()
                .post("/api/tasks/{id}/participate", taskId)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        // 7. Check volunteers list (should contain 2 volunteers)
        given()
                .when()
                .get("/api/tasks/{id}/volunteers", taskId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(2));

        // 8. First volunteer leaves the task
        asVolunteerUser()
                .when()
                .delete("/api/tasks/{id}/participate", taskId)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        // 9. Check participation status after leaving (should be false)
        asVolunteerUser()
                .when()
                .get("/api/tasks/{id}/participation-status", taskId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("false"));

        // 10. Check volunteers list (should contain 1 volunteer)
        given()
                .when()
                .get("/api/tasks/{id}/volunteers", taskId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(1));
    }

    @Test
    @DisplayName("Should prevent duplicate participation")
    void shouldPreventDuplicateParticipation() {
        // First join should succeed
        asVolunteerUser()
                .when()
                .post("/api/tasks/{id}/participate", taskId)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        // Second join should fail with conflict
        asVolunteerUser()
                .when()
                .post("/api/tasks/{id}/participate", taskId)
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body("message", containsString("already participating"));
    }

    @Test
    @DisplayName("Should prevent non-volunteers from participating")
    void shouldPreventNonVolunteersFromParticipating() {
        // SENSITIVE user cannot participate
        asSensitiveUser()
                .when()
                .post("/api/tasks/{id}/participate", taskId)
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());

        // Admin user cannot participate (assuming admin is not volunteer)
        asAdminUser()
                .when()
                .post("/api/tasks/{id}/participate", taskId)
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("Should prevent leaving when not participating")
    void shouldPreventLeavingWhenNotParticipating() {
        // Volunteer hasn't joined yet
        asVolunteerUser()
                .when()
                .delete("/api/tasks/{id}/participate", taskId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("message", containsString("not participating"));
    }

    @Test
    @DisplayName("Should prevent task author from participating in own task")
    void shouldPreventTaskAuthorFromParticipatingInOwnTask() {
        // Create a volunteer who will also be task author
        Long volunteerAuthorId = createVolunteerUser("volunteer.author@integration.test", 
                "VolAuthor", "User");

        // Create task by volunteer author
        TaskCreateRequest taskRequest = createTaskRequest();
        taskRequest.setTitle("Task by Volunteer Author");

        Long taskByVolunteerId = authenticatedRequest(createMockJwtToken("volunteer-author", "VOLUNTEER"))
                .body(taskRequest)
                .when()
                .post("/api/tasks")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .path("id");

        // Author cannot participate in own task
        authenticatedRequest(createMockJwtToken("volunteer-author", "VOLUNTEER"))
                .when()
                .post("/api/tasks/{id}/participate", taskByVolunteerId)
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body("message", containsString("own task"));
    }

    @Test
    @DisplayName("Should handle participation in completed task")
    void shouldHandleParticipationInCompletedTask() {
        // First, someone joins the task
        asVolunteerUser()
                .when()
                .post("/api/tasks/{id}/participate", taskId)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        // Task author completes the task
        asSensitiveUser()
                .when()
                .patch("/api/tasks/{id}/complete", taskId)
                .then()
                .statusCode(HttpStatus.OK.value());

        // New volunteer cannot join completed task
        authenticatedRequest(createMockJwtToken("volunteer2-user", "VOLUNTEER"))
                .when()
                .post("/api/tasks/{id}/participate", taskId)
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body("message", containsString("not open"));
    }

    @Test
    @DisplayName("Should return proper volunteer information")
    void shouldReturnProperVolunteerInformation() {
        // Volunteer joins task
        asVolunteerUser()
                .when()
                .post("/api/tasks/{id}/participate", taskId)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        // Check volunteer details in response
        given()
                .when()
                .get("/api/tasks/{id}/volunteers", taskId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(1))
                .body("[0].id", notNullValue())
                .body("[0].firstName", equalTo("Volunteer"))
                .body("[0].lastName", equalTo("User"))
                .body("[0].email", equalTo("volunteer@integration.test"))
                .body("[0].userType", equalTo("VOLUNTEER"))
                .body("[0].active", equalTo(true));
    }

    @Test
    @DisplayName("Should handle non-existent task participation")
    void shouldHandleNonExistentTaskParticipation() {
        Long nonExistentTaskId = 99999L;

        // Join non-existent task
        asVolunteerUser()
                .when()
                .post("/api/tasks/{id}/participate", nonExistentTaskId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("message", containsString("not found"));

        // Leave non-existent task
        asVolunteerUser()
                .when()
                .delete("/api/tasks/{id}/participate", nonExistentTaskId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        // Check status of non-existent task
        asVolunteerUser()
                .when()
                .get("/api/tasks/{id}/participation-status", nonExistentTaskId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        // Get volunteers of non-existent task
        given()
                .when()
                .get("/api/tasks/{id}/volunteers", nonExistentTaskId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Should require authentication for all participation endpoints")
    void shouldRequireAuthenticationForAllParticipationEndpoints() {
        // Join task without authentication
        given()
                .when()
                .post("/api/tasks/{id}/participate", taskId)
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());

        // Leave task without authentication
        given()
                .when()
                .delete("/api/tasks/{id}/participate", taskId)
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());

        // Check status without authentication
        given()
                .when()
                .get("/api/tasks/{id}/participation-status", taskId)
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());

        // Get volunteers without authentication
        given()
                .when()
                .get("/api/tasks/{id}/volunteers", taskId)
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    // Helper methods
    private Long createSensitiveUser() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setFirstName("Sensitive");
        request.setLastName("User");
        request.setEmail("sensitive@integration.test");
        request.setUserType(UserType.SENSITIVE);
        request.setPhone("+1234567890");

        Response response = asSensitiveUser()
                .body(request)
                .when()
                .post("/api/users/register")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .response();

        return response.path("id");
    }

    private Long createVolunteerUser(String email, String firstName, String lastName) {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setEmail(email);
        request.setUserType(UserType.VOLUNTEER);
        request.setPhone("+1234567890");

        String token = createMockJwtToken("volunteer-user", "VOLUNTEER");
        if (email.contains("volunteer1")) {
            token = createMockJwtToken("volunteer1-user", "VOLUNTEER");
        } else if (email.contains("volunteer2")) {
            token = createMockJwtToken("volunteer2-user", "VOLUNTEER");
        } else if (email.contains("volunteer.author")) {
            token = createMockJwtToken("volunteer-author", "VOLUNTEER");
        }

        Response response = authenticatedRequest(token)
                .body(request)
                .when()
                .post("/api/users/register")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .response();

        return response.path("id");
    }

    private Long createTestTask() {
        TaskCreateRequest request = createTaskRequest();

        Response response = asSensitiveUser()
                .body(request)
                .when()
                .post("/api/tasks")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .response();

        return response.path("id");
    }

    private TaskCreateRequest createTaskRequest() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("Integration Test Task");
        request.setDescription("This is an integration test task for participation");
        request.setLocation("Test Location");
        request.setDeadline(LocalDateTime.now().plusDays(7));
        request.setCategoryIds(Set.of(1L));
        return request;
    }
}
