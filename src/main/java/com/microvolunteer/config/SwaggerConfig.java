package com.microvolunteer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger OpenAPI configuration with Keycloak OAuth2 integration.
 */
@Configuration
public class SwaggerConfig {
    
    @Value("${app.keycloak.auth-server-url:http://localhost:8080}")
    private String keycloakServerUrl;
    
    @Value("${app.keycloak.realm:microvolunteer}")
    private String keycloakRealm;
    
    @Value("${app.keycloak.client-id:microvolunteer-app}")
    private String keycloakClientId;
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("MicroVolunteer API")
                .description("Backend API for MicroVolunteer - connecting vulnerable people with volunteers")
                .version("1.0.0")
                .contact(new Contact()
                    .name("MicroVolunteer Team")
                    .email("support@microvolunteer.com")
                    .url("https://microvolunteer.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8081")
                    .description("Local development server"),
                new Server()
                    .url("https://api.microvolunteer.com")
                    .description("Production server")))
            .addSecurityItem(new SecurityRequirement().addList("keycloak"))
            .components(new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes("keycloak", new SecurityScheme()
                    .type(SecurityScheme.Type.OAUTH2)
                    .description("Keycloak OAuth2 Authentication")
                    .flows(new OAuthFlows()
                        .authorizationCode(new OAuthFlow()
                            .authorizationUrl(keycloakServerUrl + "/realms/" + keycloakRealm + "/protocol/openid-connect/auth")
                            .tokenUrl(keycloakServerUrl + "/realms/" + keycloakRealm + "/protocol/openid-connect/token")
                            .refreshUrl(keycloakServerUrl + "/realms/" + keycloakRealm + "/protocol/openid-connect/token")
                            .scopes(new io.swagger.v3.oas.models.security.Scopes()
                                .addString("openid", "OpenID Connect scope")
                                .addString("profile", "Profile information")
                                .addString("email", "Email address"))))));
    }
}
