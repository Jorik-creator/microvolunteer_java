package com.microvolunteer.config;

import com.microvolunteer.entity.User;
import com.microvolunteer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class UserDetailsConfig {

    private final UserRepository userRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getEmail())
                    .password("") // Пароль не используется так как аутентификация через JWT
                    .authorities("ROLE_" + user.getUserType().name())
                    .accountExpired(false)
                    .accountLocked(!user.getIsActive())
                    .credentialsExpired(false)
                    .disabled(!user.getIsActive())
                    .build();
        };
    }
}
