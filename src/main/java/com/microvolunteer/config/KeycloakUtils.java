package com.microvolunteer.config;

import com.microvolunteer.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class KeycloakUtils {

    private final JwtService jwtService;

    public Optional<String> getCurrentUserKeycloakId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof String) {
            return Optional.of((String) authentication.getPrincipal());
        }

        return Optional.empty();
    }

    public Optional<String> getCurrentUserEmail() {
        return getCurrentTokenFromRequest()
                .map(jwtService::extractEmail);
    }

    public Optional<String> getCurrentUsername() {
        return getCurrentTokenFromRequest()
                .map(jwtService::extractUsername);
    }

    private Optional<String> getCurrentTokenFromRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return Optional.of(authHeader.substring(7));
            }
        }
        return Optional.empty();
    }
}