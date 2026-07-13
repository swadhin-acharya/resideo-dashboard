package com.openqa.dashboard.controller;

import com.openqa.dashboard.model.dto.UserResponse;
import com.openqa.dashboard.model.enums.GlobalRole;
import com.openqa.dashboard.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> listUsers() {
        return ResponseEntity.ok(userService.listUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody Map<String, String> body) {
        String roleStr = body.get("globalRole");
        GlobalRole role = roleStr != null ? GlobalRole.valueOf(roleStr.toUpperCase()) : null;
        return ResponseEntity.ok(userService.createUser(
                body.get("username"), body.get("email"), body.get("password"), role));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        String roleStr = body.get("globalRole");
        GlobalRole role = roleStr != null ? GlobalRole.valueOf(roleStr.toUpperCase()) : null;
        Boolean enabled = body.containsKey("enabled") ? Boolean.parseBoolean(body.get("enabled")) : null;
        return ResponseEntity.ok(userService.updateUser(id, body.get("email"), body.get("displayName"), enabled, role));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
