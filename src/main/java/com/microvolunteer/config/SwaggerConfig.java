package com.microvolunteer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8081}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("🤝 Microvolunteer API")
                        .description("""
                                ## Платформа мікроволонтерства
                                
                                API для з'єднання волонтерів та людей, які потребують допомоги.
                                
                                ### Основні функції:
                                - 👤 **Управління користувачами** - реєстрація, профілі, типи користувачів
                                - 📋 **Управління завданнями** - створення, пошук, участь у завданнях
                                - 🏷️ **Категорії завдань** - організація завдань за типами
                                - 🤝 **Система участі** - приєднання та залишення завдань
                                - 🔐 **Автентифікація** - інтеграція з Keycloak
                                
                                ### Типи користувачів:
                                - **VOLUNTEER** - волонтери, які допомагають
                                - **ORGANIZER** - організатори заходів  
                                - **AFFECTED_PERSON** - люди, які потребують допомоги
                                - **ADMIN** - адміністратори системи
                                
                                ### Авторизація:
                                Для доступу до захищених endpoints використовуйте Bearer токен у заголовку Authorization.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Microvolunteer Team")
                                .email("contact@microvolunteer.com")
                                .url("https://github.com/microvolunteer"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Локальний сервер розробки"),
                        new Server()
                                .url("https://api.microvolunteer.com")
                                .description("Продакшн сервер")
                ))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")
                                        .description("Введіть JWT токен у форматі: Bearer {токен}")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
    }
}
