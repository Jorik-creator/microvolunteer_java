package org.example.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        String httpMethod = request.getMethod();
        
        log.debug("Processing request: {} {}", httpMethod, requestPath);
        
        if (isPublicEndpoint(requestPath, httpMethod)) {
            log.debug("Skipping JWT processing for public endpoint: {} {}", httpMethod, requestPath);
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                if (tokenProvider.validateToken(jwt)) {
                    String username = tokenProvider.getUsernameFromToken(jwt);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    SecurityContextHolder.clearContext();
                }
            }
        } catch (Exception ex) {
            log.error("JWT authentication failed", ex);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean isPublicEndpoint(String requestPath, String httpMethod) {
        
        if (requestPath.startsWith("/api/auth/")) {
            return true;
        }
        
        if (requestPath.startsWith("/api/swagger-ui/") ||
            requestPath.startsWith("/api/api-docs/") ||
            requestPath.equals("/api/swagger-ui.html") ||
            requestPath.startsWith("/swagger-ui/") ||
            requestPath.startsWith("/api-docs/") ||
            requestPath.equals("/swagger-ui.html") ||
            requestPath.startsWith("/webjars/") ||
            requestPath.startsWith("/swagger-resources/")) {
            return true;
        }
        
        if (requestPath.equals("/api/actuator/health")) {
            return true;
        }
        
        if (requestPath.startsWith("/api/categories") && "GET".equals(httpMethod)) {
            return true;
        }
        
        if (requestPath.startsWith("/api/tasks") && "GET".equals(httpMethod)) {
            return true;
        }
        
        return false;
    }
}