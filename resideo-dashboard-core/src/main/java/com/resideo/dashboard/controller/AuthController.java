package com.resideo.dashboard.controller;

import com.resideo.dashboard.model.dto.ApiTokenResponse;
import com.resideo.dashboard.model.dto.LoginRequest;
import com.resideo.dashboard.model.dto.LoginResponse;
import com.resideo.dashboard.security.CurrentUser;
import com.resideo.dashboard.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(authService.register(
                    body.get("username"), body.get("email"), body.get("password")));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/tokens")
    public ResponseEntity<List<ApiTokenResponse>> listTokens(@CurrentUser UUID userId) {
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(authService.listTokens(userId));
    }

    @PostMapping("/tokens")
    public ResponseEntity<ApiTokenResponse> createToken(@CurrentUser UUID userId,
                                                         @RequestBody Map<String, Object> body) {
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String name = (String) body.getOrDefault("name", "API Token");
        String purpose = (String) body.get("purpose");
        String projectIdStr = (String) body.get("projectId");
        UUID projectId = projectIdStr != null ? UUID.fromString(projectIdStr) : null;
        Integer expiresInDays = body.get("expiresInDays") != null
                ? ((Number) body.get("expiresInDays")).intValue() : null;
        return ResponseEntity.ok(authService.createToken(userId, name, projectId, expiresInDays, purpose));
    }

    @PatchMapping("/tokens/{tokenId}")
    public ResponseEntity<ApiTokenResponse> updateToken(@CurrentUser UUID userId,
                                                        @PathVariable UUID tokenId,
                                                        @RequestBody Map<String, String> body) {
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String newName = body.get("name");
        String newPurpose = body.get("purpose");
        if (newName == null && newPurpose == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(authService.updateToken(tokenId, userId, newName, newPurpose));
    }

    @DeleteMapping("/tokens/{tokenId}")
    public ResponseEntity<Void> revokeToken(@CurrentUser UUID userId, @PathVariable UUID tokenId) {
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        authService.revokeToken(tokenId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<LoginResponse> me(@CurrentUser com.resideo.dashboard.model.entity.UserEntity user) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        LoginResponse resp = new LoginResponse();
        resp.setUserId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setDisplayName(user.getDisplayName());
        resp.setGlobalRole(user.getGlobalRole().name());
        var memberships = authService.getUserMemberships(user.getId());
        resp.setProjects(memberships);
        return ResponseEntity.ok(resp);
    }
}
