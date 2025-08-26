package org.example.service;

import org.example.dto.AuthRequest;
import org.example.dto.AuthResponse;
import org.example.dto.UserRegistrationRequest;
import org.example.exception.BadRequestException;
import org.example.model.User;
import org.example.model.UserType;
import org.example.repository.UserRepository;
import org.example.security.JwtTokenProvider;
import org.example.util.EntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private EntityMapper entityMapper;

    @InjectMocks
    private AuthService authService;

    private UserRegistrationRequest registrationRequest;
    private User user;
    private AuthRequest authRequest;

    @BeforeEach
    void setUp() {
        registrationRequest = new UserRegistrationRequest(
                "testuser", 
                "test@example.com", 
                "password123", 
                "Test", 
                "User", 
                UserType.VOLUNTEER, 
                null, 
                null, 
                null
        );

        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .userType(UserType.VOLUNTEER)
                .isActive(true)
                .build();

        authRequest = new AuthRequest("testuser", "password123");
    }

    @Test
    void register_Success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(entityMapper.toUser(any(UserRegistrationRequest.class))).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tokenProvider.generateToken(anyString())).thenReturn("jwt-token");

        AuthResponse response = authService.register(registrationRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("VOLUNTEER", response.getUserType());

        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void register_UsernameTaken_ThrowsBadRequestException() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        BadRequestException exception = assertThrows(
                BadRequestException.class, 
                () -> authService.register(registrationRequest)
        );

        assertEquals("Username is already taken!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_EmailTaken_ThrowsBadRequestException() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        BadRequestException exception = assertThrows(
                BadRequestException.class, 
                () -> authService.register(registrationRequest)
        );

        assertEquals("Email is already in use!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt-token");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        AuthResponse response = authService.login(authRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("VOLUNTEER", response.getUserType());
    }

    @Test
    void login_UserNotFound_ThrowsBadRequestException() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt-token");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(
                BadRequestException.class, 
                () -> authService.login(authRequest)
        );

        assertEquals("User not found", exception.getMessage());
    }
}