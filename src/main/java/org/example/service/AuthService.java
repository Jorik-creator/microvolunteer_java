package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.AuthRequest;
import org.example.dto.AuthResponse;
import org.example.dto.UserRegistrationRequest;
import org.example.exception.BadRequestException;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.example.security.JwtTokenProvider;
import org.example.util.EntityMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final EntityMapper entityMapper;

    @Transactional
    public AuthResponse register(UserRegistrationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use!");
        }

        User user = entityMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        String jwt = tokenProvider.generateToken(savedUser.getUsername());

        return new AuthResponse(jwt, savedUser.getUsername(), 
                               savedUser.getEmail(), savedUser.getUserType().name());
    }

    public AuthResponse login(AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getUsername(),
                        authRequest.getPassword()
                )
        );

        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(authRequest.getUsername())
                .orElseThrow(() -> new BadRequestException("User not found"));

        return new AuthResponse(jwt, user.getUsername(), 
                               user.getEmail(), user.getUserType().name());
    }

    public AuthResponse refreshToken(String token) {
        if (!tokenProvider.validateToken(token)) {
            throw new BadRequestException("Invalid or expired token");
        }

        String username = tokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found"));

        String newJwt = tokenProvider.generateToken(username);

        return new AuthResponse(newJwt, user.getUsername(), 
                               user.getEmail(), user.getUserType().name());
    }
}