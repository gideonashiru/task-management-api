package com.gideon.task_management_api.service;

import com.gideon.task_management_api.dataTransfer.AuthResponse;
import com.gideon.task_management_api.entity.User;
import com.gideon.task_management_api.exception.DuplicateResourceException;
import com.gideon.task_management_api.exception.InvalidCredentialsException;
import com.gideon.task_management_api.repository.UserRepository;
import com.gideon.task_management_api.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Test
    void register_shouldThrowException_whenUsernameAlreadyExists() {
        // Arrange
        User existingUser = User.builder()
                .username("gideon")
                .passwordHash("hashedpassword")
                .name("Gideon")
                .build();

        when(userRepository.findByUsername("gideon"))
                .thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            userService.register("gideon", "password123", "Gideon");
        });

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldReturnCorrectCredentials_whenUserSuccessfullyRegistered() {
        // Arrange
        User newUser = User.builder()
                .username("gideon")
                .passwordHash("hashedpassword")
                .name("Gideon")
                .build();

        when(passwordEncoder.encode("password123")).thenReturn("hashedpassword");
        when(userRepository.findByUsername("gideon")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(jwtUtil.generateToken(any(User.class))).thenReturn("test-token");

        // Act
        AuthResponse result = userService.register("gideon", "password123", "Gideon");

        assertEquals("test-token", result.token());
        assertEquals("gideon", result.username());
        assertEquals("Gideon", result.name());
        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtUtil).generateToken(newUser);
    }

    @Test
    void login_shouldThrow_whenUsernameDoesNotExist() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class, () ->
                userService.login("unknown", "anyPassword"));

        assertEquals("Invalid username or password", ex.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_shouldThrow_whenPasswordDoesNotMatch() {
        User user = User.builder()
                .username("gideon")
                .passwordHash("storedHash")
                .name("Gideon")
                .build();
        when(userRepository.findByUsername("gideon")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "storedHash")).thenReturn(false);

        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class, () ->
                userService.login("gideon", "wrongPassword"));

        assertEquals("Invalid username or password", ex.getMessage());
        verify(passwordEncoder).matches("wrongPassword", "storedHash");
    }

    @Test
    void login_shouldReturnUser_whenUsernameExistsAndPasswordMatches() {
        User user = User.builder()
                .username("gideon")
                .passwordHash("storedHash")
                .name("Gideon")
                .build();
        when(userRepository.findByUsername("gideon")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctPassword", "storedHash")).thenReturn(true);
        when(jwtUtil.generateToken(user)).thenReturn("test-token");

        AuthResponse result = userService.login("gideon", "correctPassword");

        assertEquals("test-token", result.token());
        assertEquals("gideon", result.username());
        assertEquals("Gideon", result.name());
        verify(passwordEncoder).matches("correctPassword", "storedHash");
        verify(jwtUtil).generateToken(user);
    }
}