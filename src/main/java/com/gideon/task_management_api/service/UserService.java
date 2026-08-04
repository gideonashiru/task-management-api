package com.gideon.task_management_api.service;

import com.gideon.task_management_api.dataTransfer.AuthResponse;
import com.gideon.task_management_api.entity.User;
import com.gideon.task_management_api.exception.DuplicateResourceException;
import com.gideon.task_management_api.exception.InvalidCredentialsException;
import com.gideon.task_management_api.repository.UserRepository;
import com.gideon.task_management_api.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(String username, String password, String name) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new DuplicateResourceException("Username already exists");
        }

        String hashedPassword = passwordEncoder.encode(password);
        User user = User.builder()
                .username(username)
                .passwordHash(hashedPassword)
                .name(name)
                .build();
        User savedUser = userRepository.save(user);

        return AuthResponse.from(savedUser, jwtUtil.generateToken(savedUser));
    }

    public AuthResponse login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
        return AuthResponse.from(user, jwtUtil.generateToken(user));
    }
}