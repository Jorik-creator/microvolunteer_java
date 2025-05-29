package com.microvolunteer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MicrovolunteerApplicationTests {

    @Test
    void contextLoads() {
        // Тест перевіряє, що контекст Spring Boot завантажується без помилок
    }
}
