package com.microvolunteer.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Component
public class KeycloakUtils {

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    public String createUser(String email, String password, String firstName, String lastName) {
        log.info("Creating user in Keycloak: {}", email);
        
        try {
            Keycloak keycloak = KeycloakBuilder.builder()
                    .serverUrl(keycloakServerUrl)
                    .realm("master")
                    .username(adminUsername)
                    .password(adminPassword)
                    .clientId("admin-cli")
                    .build();

            UserRepresentation user = new UserRepresentation();
            user.setUsername(email);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEnabled(true);
            user.setEmailVerified(true);

            CredentialRepresentation passwordCred = new CredentialRepresentation();
            passwordCred.setTemporary(false);
            passwordCred.setType(CredentialRepresentation.PASSWORD);
            passwordCred.setValue(password);
            user.setCredentials(Arrays.asList(passwordCred));

            var response = keycloak.realm(realm).users().create(user);
            
            if (response.getStatus() == 201) {
                String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
                log.info("User created successfully in Keycloak with ID: {}", userId);
                return userId;
            } else {
                throw new RuntimeException("Failed to create user in Keycloak: " + response.getStatus());
            }
        } catch (Exception e) {
            log.error("Error creating user in Keycloak", e);
            throw new RuntimeException("Failed to create user in Keycloak", e);
        }
    }

    public UserInfo getUserInfo(String keycloakUserId) {
        try {
            Keycloak keycloak = KeycloakBuilder.builder()
                    .serverUrl(keycloakServerUrl)
                    .realm("master")
                    .username(adminUsername)
                    .password(adminPassword)
                    .clientId("admin-cli")
                    .build();

            UserRepresentation user = keycloak.realm(realm).users().get(keycloakUserId).toRepresentation();
            
            return UserInfo.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .enabled(user.isEnabled())
                    .build();
        } catch (Exception e) {
            log.error("Error getting user info from Keycloak", e);
            throw new RuntimeException("Failed to get user info from Keycloak", e);
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private String id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private Boolean enabled;
    }
}
