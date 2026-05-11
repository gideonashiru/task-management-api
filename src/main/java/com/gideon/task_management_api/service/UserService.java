package com.gideon.task_management_api.service;

import com.gideon.task_management_api.entity.User;
import com.gideon.task_management_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(String username, String password, String name) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        // save logic comes after the test passes
        String hashedPassword = passwordEncoder.encode(password);
        User user = User.builder()
                .username(username)
                .passwordHash(hashedPassword)
                .name(name)
                .build();
        userRepository.save(user);
        return user;
    }

   public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        return user;
    }
}