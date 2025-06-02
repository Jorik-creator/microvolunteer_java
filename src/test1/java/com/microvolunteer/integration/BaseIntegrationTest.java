package com.microvolunteer.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests with Testcontainers
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("integration-test")
public abstract class BaseIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgresql = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("microvolunteer_test")
            .withUsername("test_user")
            .withPassword("test_password")
            .withReuse(true);

    @LocalServerPort
    protected int port;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresql::getJdbcUrl);
        registry.add("spring.datasource.username", postgresql::getUsername);
        registry.add("spring.datasource.password", postgresql::getPassword);
        
        // Disable security for integration tests
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> "");
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> "");
    }

    @BeforeEach
    void setUpRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    /**
     * Create a mock JWT token for testing
     */
    protected String createMockJwtToken(String subject, String role) {
        // In real tests, this would create a proper JWT token
        // For simplicity, we'll return a mock token
        return "Bearer mock-jwt-token-" + subject + "-" + role;
    }

    /**
     * Helper method to make authenticated requests
     */
    protected io.restassured.specification.RequestSpecification authenticatedRequest(String token) {
        return RestAssured.given()
                .header("Authorization", token)
                .contentType(ContentType.JSON);
    }

    /**
     * Helper method to make requests as SENSITIVE user
     */
    protected io.restassured.specification.RequestSpecification asSensitiveUser() {
        return authenticatedRequest(createMockJwtToken("sensitive-user", "SENSITIVE"));
    }

    /**
     * Helper method to make requests as VOLUNTEER user
     */
    protected io.restassured.specification.RequestSpecification asVolunteerUser() {
        return authenticatedRequest(createMockJwtToken("volunteer-user", "VOLUNTEER"));
    }

    /**
     * Helper method to make requests as ADMIN user
     */
    protected io.restassured.specification.RequestSpecification asAdminUser() {
        return authenticatedRequest(createMockJwtToken("admin-user", "ADMIN"));
    }
}
