package com.kinukollu.backend.controller;

import com.kinukollu.backend.dto.LoginRequest;
import com.kinukollu.backend.dto.SignupRequest;
import com.kinukollu.backend.entity.User;
import com.kinukollu.backend.repository.UserRepository;
import com.kinukollu.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest();
        signupRequest.setEmail("newuser@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setFullName("New User");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("existing@example.com");
        loginRequest.setPassword("correctpassword");
    }

    @Test
    void signup_shouldRejectDuplicateEmail() {
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(true);

        ResponseEntity<?> response = authController.signup(signupRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void signup_shouldCreateUserAndReturnToken_whenEmailIsNew() {
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("hashed-password");
        when(jwtUtil.generateToken(signupRequest.getEmail())).thenReturn("fake-jwt-token");

        ResponseEntity<?> response = authController.signup(signupRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void login_shouldRejectNonExistentUser() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        ResponseEntity<?> response = authController.login(loginRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void login_shouldRejectWrongPassword() {
        User existingUser = new User();
        existingUser.setEmail(loginRequest.getEmail());
        existingUser.setPasswordHash("stored-hash");

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), existingUser.getPasswordHash())).thenReturn(false);

        ResponseEntity<?> response = authController.login(loginRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void login_shouldSucceedWithCorrectCredentials() {
        User existingUser = new User();
        existingUser.setEmail(loginRequest.getEmail());
        existingUser.setPasswordHash("stored-hash");
        existingUser.setFullName("Existing User");

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), existingUser.getPasswordHash())).thenReturn(true);
        when(jwtUtil.generateToken(existingUser.getEmail())).thenReturn("fake-jwt-token");

        ResponseEntity<?> response = authController.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
