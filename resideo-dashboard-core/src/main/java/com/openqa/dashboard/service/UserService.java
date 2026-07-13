package com.openqa.dashboard.service;

import com.openqa.dashboard.model.dto.UserResponse;
import com.openqa.dashboard.model.entity.UserEntity;
import com.openqa.dashboard.model.enums.GlobalRole;
import com.openqa.dashboard.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    public UserResponse getUser(UUID id) {
        return userRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserResponse createUser(String username, String email, String password, GlobalRole role) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already taken");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already taken");
        }

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setEmail(email);
        if (password != null) {
            user.setPasswordHash(passwordEncoder.encode(password));
        }
        user.setDisplayName(username);
        user.setGlobalRole(role != null ? role : GlobalRole.USER);
        user.setEnabled(true);
        user = userRepository.save(user);
        return UserResponse.from(user);
    }

    public UserResponse updateUser(UUID id, String email, String displayName, Boolean enabled, GlobalRole role) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (email != null) user.setEmail(email);
        if (displayName != null) user.setDisplayName(displayName);
        if (enabled != null) user.setEnabled(enabled);
        if (role != null) user.setGlobalRole(role);
        user = userRepository.save(user);
        return UserResponse.from(user);
    }

    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }
}
