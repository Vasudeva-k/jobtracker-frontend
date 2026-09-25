package com.vasudev.jobtracker.service;

import com.vasudev.jobtracker.dto.LoginRequest;
import com.vasudev.jobtracker.dto.LoginResponse;
import com.vasudev.jobtracker.dto.RegisterRequest;
import com.vasudev.jobtracker.entity.User;
import com.vasudev.jobtracker.repository.UserRepository;
import com.vasudev.jobtracker.security.JwtService;
import com.vasudev.jobtracker.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User activeUser;
    private User blockedUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFirstName("Jane");
        registerRequest.setLastName("Doe");
        registerRequest.setEmail("jane.doe@example.com");
        registerRequest.setPassword("securePassword123");
        registerRequest.setPhone("+1 555 1234");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("jane.doe@example.com");
        loginRequest.setPassword("securePassword123");

        activeUser = User.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .password("$2a$10$encodedPassword")
                .role("USER")
                .active(true)
                .build();

        blockedUser = User.builder()
                .id(2L)
                .firstName("Blocked")
                .lastName("User")
                .email("blocked@example.com")
                .password("$2a$10$encodedPassword")
                .role("USER")
                .active(false)
                .build();
    }

    @Test
    void testRegisterUserSuccess() {
        when(userRepository.existsByEmail("jane.doe@example.com")).thenReturn(false);
        when(passwordEncoder.encode("securePassword123")).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(activeUser);

        String result = userService.registerUser(registerRequest);

        assertEquals("User Registered Successfully!", result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUserDuplicateEmailThrowsException() {
        when(userRepository.existsByEmail("jane.doe@example.com")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser(registerRequest)
        );

        assertTrue(exception.getMessage().contains("Email already exists"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLoginUserSuccess() {
        when(userRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.of(activeUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(jwtService.generateToken("jane.doe@example.com")).thenReturn("mocked-jwt-token");

        LoginResponse response = userService.loginUser(loginRequest);

        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.getToken());
        assertEquals("jane.doe@example.com", response.getEmail());
        assertEquals("Jane", response.getFirstName());
        assertEquals("USER", response.getRole());
        assertTrue(response.getActive());
    }

    @Test
    void testLoginBlockedUserThrowsException() {
        loginRequest.setEmail("blocked@example.com");
        when(userRepository.findByEmail("blocked@example.com")).thenReturn(Optional.of(blockedUser));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.loginUser(loginRequest)
        );

        assertTrue(exception.getMessage().contains("deactivated or blocked"));
        verify(authenticationManager, never()).authenticate(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void testLoginBadCredentialsThrowsException() {
        when(userRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.of(activeUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(
                BadCredentialsException.class,
                () -> userService.loginUser(loginRequest)
        );
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void testLoginUserNotFoundThrowsException() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        loginRequest.setEmail("nonexistent@example.com");

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> userService.loginUser(loginRequest)
        );

        assertTrue(exception.getMessage().contains("Invalid email or password"));
    }
}
