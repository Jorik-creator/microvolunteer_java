package com.microvolunteer.config;

import com.microvolunteer.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakUtils {

    private final JwtService jwtService;

    /**
     * Отримує Keycloak ID поточного користувача
     */
    public Optional<String> getCurrentUserKeycloakId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && 
            authentication.getPrincipal() instanceof String) {
            return Optional.of((String) authentication.getPrincipal());
        }

        return Optional.empty();
    }

    /**
     * Отримує email поточного користувача з JWT токена
     */
    public Optional<String> getCurrentUserEmail() {
        return getCurrentTokenFromRequest()
                .map(jwtService::extractEmail);
    }

    /**
     * Отримує username поточного користувача з JWT токена
     */
    public Optional<String> getCurrentUsername() {
        return getCurrentTokenFromRequest()
                .map(jwtService::extractUsername);
    }

    /**
     * Отримує повне ім'я поточного користувача з JWT токена
     */
    public Optional<String> getCurrentUserFullName() {
        return getCurrentTokenFromRequest()
                .map(jwtService::extractFullName);
    }

    /**
     * Отримує ролі поточного користувача
     */
    public List<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                    .collect(Collectors.toList());
        }
        
        return List.of();
    }

    /**
     * Перевіряє, чи має користувач конкретну роль
     */
    public boolean hasRole(String roleName) {
        List<String> roles = getCurrentUserRoles();
        return roles.contains(roleName.toUpperCase()) || roles.contains(roleName);
    }

    /**
     * Перевіряє, чи має користувач будь-яку з вказаних ролей
     */
    public boolean hasAnyRole(String... roleNames) {
        List<String> userRoles = getCurrentUserRoles();
        for (String roleName : roleNames) {
            if (userRoles.contains(roleName.toUpperCase()) || userRoles.contains(roleName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Отримує весь JWT токен з поточного запиту
     */
    public Optional<String> getCurrentToken() {
        return getCurrentTokenFromRequest();
    }

    /**
     * Отримує всю інформацію про користувача з JWT токена
     */
    public Optional<UserInfo> getCurrentUserInfo() {
        return getCurrentTokenFromRequest().map(token -> {
            try {
                return UserInfo.builder()
                        .keycloakId(jwtService.extractKeycloakId(token))
                        .username(jwtService.extractUsername(token))
                        .email(jwtService.extractEmail(token))
                        .fullName(jwtService.extractFullName(token))
                        .roles(jwtService.extractRoles(token))
                        .build();
            } catch (Exception e) {
                log.error("Помилка витягування інформації з токена: {}", e.getMessage());
                return null;
            }
        });
    }

    /**
     * Перевіряє, чи користувач аутентифікований
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && 
               !authentication.getPrincipal().equals("anonymousUser");
    }

    /**
     * Витягує JWT токен з поточного HTTP запиту
     */
    private Optional<String> getCurrentTokenFromRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader("Authorization");

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    return Optional.of(authHeader.substring(7));
                }
            }
        } catch (Exception e) {
            log.debug("Не вдалося отримати токен з запиту: {}", e.getMessage());
        }
        
        return Optional.empty();
    }

    /**
     * Клас для зберігання інформації про користувача
     */
    public static class UserInfo {
        private final String keycloakId;
        private final String username;
        private final String email;
        private final String fullName;
        private final List<String> roles;

        private UserInfo(Builder builder) {
            this.keycloakId = builder.keycloakId;
            this.username = builder.username;
            this.email = builder.email;
            this.fullName = builder.fullName;
            this.roles = builder.roles;
        }

        public static Builder builder() {
            return new Builder();
        }

        // Getters
        public String getKeycloakId() { return keycloakId; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getFullName() { return fullName; }
        public List<String> getRoles() { return roles; }

        public static class Builder {
            private String keycloakId;
            private String username;
            private String email;
            private String fullName;
            private List<String> roles;

            public Builder keycloakId(String keycloakId) {
                this.keycloakId = keycloakId;
                return this;
            }

            public Builder username(String username) {
                this.username = username;
                return this;
            }

            public Builder email(String email) {
                this.email = email;
                return this;
            }

            public Builder fullName(String fullName) {
                this.fullName = fullName;
                return this;
            }

            public Builder roles(List<String> roles) {
                this.roles = roles;
                return this;
            }

            public UserInfo build() {
                return new UserInfo(this);
            }
        }
    }
}