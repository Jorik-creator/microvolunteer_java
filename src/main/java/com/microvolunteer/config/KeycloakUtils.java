package com.microvolunteer.config;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class KeycloakUtils {

    public Optional<String> getCurrentUserKeycloakId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof KeycloakPrincipal) {
            KeycloakPrincipal<?> keycloakPrincipal = (KeycloakPrincipal<?>) authentication.getPrincipal();
            return Optional.of(keycloakPrincipal.getName());
        }

        return Optional.empty();
    }

    public Optional<AccessToken> getCurrentUserToken() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof KeycloakPrincipal) {
            KeycloakPrincipal<?> keycloakPrincipal = (KeycloakPrincipal<?>) authentication.getPrincipal();
            KeycloakSecurityContext context = keycloakPrincipal.getKeycloakSecurityContext();
            return Optional.of(context.getToken());
        }

        return Optional.empty();
    }

    public Optional<String> getCurrentUserEmail() {
        return getCurrentUserToken().map(AccessToken::getEmail);
    }

    public Optional<String> getCurrentUsername() {
        return getCurrentUserToken().map(AccessToken::getPreferredUsername);
    }
}