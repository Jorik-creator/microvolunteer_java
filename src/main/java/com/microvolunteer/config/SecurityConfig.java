package com.microvolunteer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Security configuration for the application.
 * Configures JWT authentication with Keycloak as the OAuth2 resource server.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    /**
     * Configure security filter chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // 1️⃣ – Stateless API; сесію не створюємо
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 2️⃣ – CSRF не потрібен для чистого REST (за потреби заберіть disable)
                .csrf(csrf -> csrf.disable())

                // 4️⃣ – Авторизація за маршрутами
                .authorizeHttpRequests(authorize -> authorize

                        // ▸ Swagger-UI, OpenAPI, Actuator
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/actuator/health"
                        ).permitAll()

                        // ▸ Публичні ресурси (якщо є)
                        // .requestMatchers("/public/**").permitAll()

                        // ▸ Завдання: лише для ADMIN
                        .requestMatchers(HttpMethod.GET,    "/api/tasks/**").hasAnyRole("ADMIN", "VOLUNTEER")
                        .requestMatchers(HttpMethod.POST,   "/api/tasks").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH,  "/api/tasks/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/tasks/**").hasRole("ADMIN")

                        // ▸ Будь-що інше — тільки з валідним токеном
                        .anyRequest().authenticated()
                )

                // 5️⃣ – Ресурс-сервер: приймаємо та перевіряємо JWT від Keycloak
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(Customizer.withDefaults())
                );

        return http.build();
    }
    
    /**
     * Configure JWT authentication converter to extract roles from Keycloak token
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Extract realm_access roles from Keycloak JWT token
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            
            if (realmAccess == null) {
                return List.of();
            }
            
            @SuppressWarnings("unchecked")
            Collection<String> roles = (Collection<String>) realmAccess.get("roles");
            
            if (roles == null) {
                return List.of();
            }
            
            return roles.stream()
                .filter(role -> role.startsWith("ROLE_"))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        });
        
        // Set principal claim name (Keycloak uses 'sub' by default)
        converter.setPrincipalClaimName("sub");
        
        return converter;
    }
    
    /**
     * Configure CORS for frontend integration
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow specific origins in production, for development allow all
        configuration.setAllowedOriginPatterns(List.of(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "https://*.yourdomain.com"
        ));
        
        configuration.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        
        configuration.setAllowedHeaders(List.of(
            "authorization", "content-type", "x-auth-token"
        ));
        
        configuration.setExposedHeaders(List.of(
            "x-auth-token"
        ));
        
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
