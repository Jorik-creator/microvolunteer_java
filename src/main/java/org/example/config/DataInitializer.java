package org.example.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.*;
import org.example.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.data.init", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final DataSource dataSource;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (shouldInitializeData()) {
            log.info("Starting database initialization with test data...");
            
            initializeCategories();
            initializeUsers();
            initializeTasks();
            
            log.info("Database initialization completed successfully!");
        } else {
            log.info("Database already contains data, skipping initialization");
        }
    }

    private boolean shouldInitializeData() {
        return categoryRepository.count() == 0 && userRepository.count() == 0;
    }

    private void initializeCategories() {
        log.info("Loading categories from SQL file...");
        executeSqlFile("data/test-categories.sql");
        log.info("Successfully loaded {} categories", categoryRepository.count());
    }

    private void initializeUsers() {
        log.info("Loading users from SQL file...");
        executeSqlFile("data/test-users.sql");
        log.info("Successfully loaded {} users", userRepository.count());
    }

    private void initializeTasks() {
        log.info("Loading tasks from SQL file...");
        executeSqlFile("data/simple-tasks.sql");
        log.info("Successfully loaded {} tasks", taskRepository.count());
    }

    private void executeSqlFile(String fileName) {
        try {
            ClassPathResource resource = new ClassPathResource(fileName);
            if (!resource.exists()) {
                log.warn("SQL file {} not found, skipping", fileName);
                return;
            }

            StringBuilder sqlContent = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("--")) {
                        sqlContent.append(line).append(" ");
                    }
                }
            }

            String sql = sqlContent.toString().trim();
            if (!sql.isEmpty()) {
                try (Connection connection = dataSource.getConnection();
                     Statement statement = connection.createStatement()) {
                    
                    String[] statements = sql.split(";");
                    for (String stmt : statements) {
                        stmt = stmt.trim();
                        if (!stmt.isEmpty()) {
                            statement.execute(stmt);
                        }
                    }
                }
            }
            
            log.info("Successfully executed SQL file: {}", fileName);
        } catch (Exception e) {
            log.error("Failed to execute SQL file: {}", fileName, e);
            throw new RuntimeException("Failed to initialize data from " + fileName, e);
        }
    }
}