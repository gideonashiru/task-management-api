package com.gideon.task_management_api.service;

import com.gideon.task_management_api.dataTransfer.AuthResponse;
import com.gideon.task_management_api.entity.User;
import com.gideon.task_management_api.repository.UserRepository;
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
        assertThrows(IllegalArgumentException.class, () -> {
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
        
        // Act
        AuthResponse result = userService.register("gideon", "password123", "Gideon");
        assertEquals("gideon", result.username());
        assertEquals("hashedpassword", result.getPasswordHash());
        assertEquals("Gideon", result.name());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void login_shouldThrow_whenUsernameDoesNotExist() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
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

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
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

        AuthResponse result = userService.login("gideon", "correctPassword");

        assertSame(user, result);
        verify(passwordEncoder).matches("correctPassword", "storedHash");
    }
}