package com.microvolunteer.config;

import com.microvolunteer.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            
            if (jwtService.isTokenValid(jwt)) {
                // Витягуємо інформацію з Keycloak токена
                final String keycloakId = jwtService.extractKeycloakId(jwt);
                final List<String> roles = jwtService.extractRoles(jwt);
                
                if (keycloakId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    
                    // Конвертуємо ролі в Spring Security authorities
                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                            .collect(Collectors.toList());
                    
                    // Якщо немає ролей, додаємо базову роль USER
                    if (authorities.isEmpty()) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    }

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            keycloakId, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    log.debug("Аутентифіковано користувача {} з ролями: {}", keycloakId, roles);
                }
            }
        } catch (Exception e) {
            log.error("Помилка аутентифікації JWT токена: {}", e.getMessage());
            // Не встановлюємо аутентифікацію при помилці
        }

        filterChain.doFilter(request, response);
    }
}