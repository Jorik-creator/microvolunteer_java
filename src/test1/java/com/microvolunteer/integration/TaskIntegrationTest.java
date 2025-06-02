package com.microvolunteer.integration;

import com.microvolunteer.dto.request.TaskCreateRequest;
import com.microvolunteer.dto.request.UserRegistrationRequest;
import com.microvolunteer.enums.TaskStatus;
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

@DisplayName("Task Integration Tests")
class TaskIntegrationTest extends BaseIntegrationTest {

    private Long sensitiveUserId;
    private Long volunteerUserId;
    private Long taskId;

    @BeforeEach
    void setUp() {
        // Create test users
        sensitiveUserId = createSensitiveUser();
        volunteerUserId = createVolunteerUser();
        
        // Create a test task
        taskId = createTestTask();
    }

    @Test
    @DisplayName("Complete task workflow: create, search, participate, complete")
    void shouldCompleteTaskWorkflowSuccessfully() {
        // 1. Search for available tasks
        given()
                .when()
                .get("/api/tasks")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content", hasSize(greaterThan(0)))
                .body("content[0].id", notNullValue())
                .body("content[0].status", equalTo("OPEN"));

        // 2. Get specific task details
        given()
                .when()
                .get("/api/tasks/{id}", taskId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(taskId.intValue()))
                .body("title", equalTo("Integration Test Task"))
                .body("status", equalTo("OPEN"))
                .body("participantsCount", equalTo(0));

        // 3. Volunteer participates in task
        asVolunteerUser()
                .when()
                .post("/api/tasks/{id}/participate", taskId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("message", containsString("successfully"));

        // 4. Check task status changed to IN_PROGRESS
        given()
                .when()
                .get("/api/tasks/{id}", taskId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("status", equalTo("IN_PROGRESS"))
                .body("participantsCount", equalTo(1));

        // 5. Author completes the task
        asSensitiveUser()
                .when()
                .patch("/api/tasks/{id}/complete", taskId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("status", equalTo("COMPLETED"))
                .body("completedAt", notNullValue());

        // 6. Verify completed task cannot be completed again
        asSensitiveUser()
                .when()
                .patch("/api/tasks/{id}/complete", taskId)
                .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    @DisplayName("Should handle task creation and filtering")
    void shouldHandleTaskCreationAndFiltering() {
        // Create another task with different category
        TaskCreateRequest secondTaskRequest = createTaskRequest();
        secondTaskRequest.setTitle("Another Test Task");
        secondTaskRequest.setDescription("Another test description");
        secondTaskRequest.setCategoryIds(Set.of(2L)); // Different category

        Long secondTaskId = asSensitiveUser()
                .body(secondTaskRequest)
                .when()
                .post("/api/tasks")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .path("id");

        // Search by status
        given()
                .param("status", "OPEN")
                .when()
                .get("/api/tasks")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content", hasSize(greaterThanOrEqualTo(2)));

        // Search by text
        given()
                .param("searchText", "Integration")
                .when()
                .get("/api/tasks")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content", hasSize(greaterThanOrEqualTo(1)))
                .body("content[0].title", containsString("Integration"));

        // Search with pagination
        given()
                .param("page", "0")
                .param("size", "1")
                .when()
                .get("/api/tasks")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content", hasSize(1))
                .body("totalElements", greaterThanOrEqualTo(2))
                .body("totalPages", greaterThanOrEqualTo(2));
    }

    @Test
    @DisplayName("Should handle authorization correctly")
    void shouldHandleAuthorizationCorrectly() {
        // Only SENSITIVE users can create tasks
        asVolunteerUser()
                .body(createTaskRequest())
                .when()
                .post("/api/tasks")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());

        // Only task author can complete task
        asVolunteerUser()
                .when()
                .patch("/api/tasks/{id}/complete", taskId)
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());

        // Only task author or admin can delete task
        asVolunteerUser()
                .when()
                .delete("/api/tasks/{id}", taskId)
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("Should handle task deletion correctly")
    void shouldHandleTaskDeletionCorrectly() {
        // Create a new task for deletion test
        Long deletableTaskId = asSensitiveUser()
                .body(createTaskRequest())
                .when()
                .post("/api/tasks")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .path("id");

        // Task can be deleted when no participants
        asSensitiveUser()
                .when()
                .delete("/api/tasks/{id}", deletableTaskId)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        // Verify task is deleted
        given()
                .when()
                .get("/api/tasks/{id}", deletableTaskId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Should prevent deletion of task with participants")
    void shouldPreventDeletionOfTaskWithParticipants() {
        // Volunteer participates in task
        asVolunteerUser()
                .when()
                .post("/api/tasks/{id}/participate", taskId)
                .then()
                .statusCode(HttpStatus.OK.value());

        // Cannot delete task with active participants
        asSensitiveUser()
                .when()
                .delete("/api/tasks/{id}", taskId)
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body("message", containsString("participants"));
    }

    @Test
    @DisplayName("Should get task statistics")
    void shouldGetTaskStatistics() {
        given()
                .when()
                .get("/api/tasks/statistics")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("total", greaterThanOrEqualTo(1))
                .body("open", greaterThanOrEqualTo(1))
                .body("inProgress", greaterThanOrEqualTo(0))
                .body("completed", greaterThanOrEqualTo(0));
    }

    @Test
    @DisplayName("Should get recent tasks")
    void shouldGetRecentTasks() {
        given()
                .param("days", "30")
                .when()
                .get("/api/tasks/recent")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("Should get my tasks")
    void shouldGetMyTasks() {
        asSensitiveUser()
                .when()
                .get("/api/tasks/my")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(greaterThanOrEqualTo(1)))
                .body("[0].id", equalTo(taskId.intValue()));
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

    private Long createVolunteerUser() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setFirstName("Volunteer");
        request.setLastName("User");
        request.setEmail("volunteer@integration.test");
        request.setUserType(UserType.VOLUNTEER);
        request.setPhone("+1234567891");

        Response response = asVolunteerUser()
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
        request.setDescription("This is an integration test task");
        request.setLocation("Test Location");
        request.setDeadline(LocalDateTime.now().plusDays(7));
        request.setCategoryIds(Set.of(1L));
        return request;
    }
}
