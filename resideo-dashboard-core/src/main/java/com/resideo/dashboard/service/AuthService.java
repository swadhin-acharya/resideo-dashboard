package com.resideo.dashboard.service;

import com.resideo.dashboard.model.dto.ApiTokenResponse;
import com.resideo.dashboard.model.dto.LoginRequest;
import com.resideo.dashboard.model.dto.LoginResponse;
import com.resideo.dashboard.model.entity.ApiToken;
import com.resideo.dashboard.model.entity.ProjectMembership;
import com.resideo.dashboard.model.entity.UserEntity;
import com.resideo.dashboard.model.enums.GlobalRole;
import com.resideo.dashboard.repository.ApiTokenRepository;
import com.resideo.dashboard.repository.ProjectMembershipRepository;
import com.resideo.dashboard.repository.ProjectRepository;
import com.resideo.dashboard.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final ApiTokenRepository apiTokenRepository;
    private final ProjectMembershipRepository membershipRepository;
    private final ProjectRepository projectRepository;
    private final PasswordEncoder passwordEncoder;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;

    public AuthService(UserRepository userRepository, ApiTokenRepository apiTokenRepository,
                       ProjectMembershipRepository membershipRepository,
                       ProjectRepository projectRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.apiTokenRepository = apiTokenRepository;
        this.membershipRepository = membershipRepository;
        this.projectRepository = projectRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!user.getEnabled()) {
            throw new RuntimeException("Account disabled");
        }

        if (user.getPasswordHash() != null && !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        String token = generateToken();
        String tokenHash = sha256(token);
        String prefix = token.substring(0, 8);

        ApiToken apiToken = new ApiToken();
        apiToken.setUserId(user.getId());
        apiToken.setName("web-session");
        apiToken.setTokenHash(tokenHash);
        apiToken.setTokenPrefix(prefix);
        apiToken.setEnabled(true);
        apiTokenRepository.save(apiToken);

        return buildLoginResponse(user, token);
    }

    public LoginResponse register(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already taken");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already taken");
        }

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setDisplayName(username);
        user.setGlobalRole(GlobalRole.USER);
        user.setEnabled(true);
        user = userRepository.save(user);

        String token = generateToken();
        String tokenHash = sha256(token);

        ApiToken apiToken = new ApiToken();
        apiToken.setUserId(user.getId());
        apiToken.setName("web-session");
        apiToken.setTokenHash(tokenHash);
        apiToken.setTokenPrefix(token.substring(0, 8));
        apiToken.setEnabled(true);
        apiTokenRepository.save(apiToken);

        return buildLoginResponse(user, token);
    }

    public List<ApiTokenResponse> listTokens(UUID userId) {
        return apiTokenRepository.findByUserId(userId).stream()
                .map(ApiTokenResponse::from)
                .collect(Collectors.toList());
    }

    public ApiTokenResponse createToken(UUID userId, String name, UUID projectId) {
        return createToken(userId, name, projectId, null, null);
    }

    public ApiTokenResponse createToken(UUID userId, String name, UUID projectId, Integer expiresInDays) {
        return createToken(userId, name, projectId, expiresInDays, null);
    }

    public ApiTokenResponse createToken(UUID userId, String name, UUID projectId, Integer expiresInDays, String purpose) {
        String token = generateToken();
        String hash = sha256(token);
        String prefix = token.substring(0, 8);

        ApiToken apiToken = new ApiToken();
        apiToken.setUserId(userId);
        apiToken.setProjectId(projectId);
        apiToken.setName(name);
        apiToken.setPurpose(purpose);
        apiToken.setTokenHash(hash);
        apiToken.setTokenPrefix(prefix);
        apiToken.setEnabled(true);
        if (expiresInDays != null && expiresInDays > 0) {
            apiToken.setExpiresAt(Instant.now().plus(java.time.Duration.ofDays(expiresInDays)));
        }
        apiToken = apiTokenRepository.save(apiToken);

        ApiTokenResponse resp = ApiTokenResponse.from(apiToken);
        resp.setFullToken(token);
        return resp;
    }

    public ApiTokenResponse updateToken(UUID tokenId, UUID userId, String newName, String newPurpose) {
        ApiToken token = apiTokenRepository.findById(tokenId)
                .orElseThrow(() -> new RuntimeException("Token not found"));
        if (!token.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        if (newName != null) token.setName(newName);
        if (newPurpose != null) token.setPurpose(newPurpose);
        token = apiTokenRepository.save(token);
        return ApiTokenResponse.from(token);
    }

    public List<LoginResponse.ProjectInfo> getUserMemberships(UUID userId) {
        return membershipRepository.findByUserId(userId).stream().map(m -> {
            LoginResponse.ProjectInfo pi = new LoginResponse.ProjectInfo();
            pi.setId(m.getProjectId());
            pi.setRole(m.getRole().name());
            projectRepository.findById(m.getProjectId()).ifPresent(p -> {
                pi.setName(p.getName());
                pi.setSlug(p.getSlug());
            });
            return pi;
        }).collect(java.util.stream.Collectors.toList());
    }

    public void revokeToken(UUID tokenId, UUID userId) {
        ApiToken token = apiTokenRepository.findById(tokenId)
                .orElseThrow(() -> new RuntimeException("Token not found"));
        if (!token.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        token.setEnabled(false);
        apiTokenRepository.save(token);
    }

    private LoginResponse buildLoginResponse(UserEntity user, String token) {
        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setUserId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setDisplayName(user.getDisplayName());
        resp.setGlobalRole(user.getGlobalRole().name());

        List<ProjectMembership> memberships = membershipRepository.findByUserId(user.getId());
        List<LoginResponse.ProjectInfo> projects = memberships.stream().map(m -> {
            LoginResponse.ProjectInfo pi = new LoginResponse.ProjectInfo();
            pi.setId(m.getProjectId());
            pi.setRole(m.getRole().name());
            projectRepository.findById(m.getProjectId()).ifPresent(p -> {
                pi.setName(p.getName());
                pi.setSlug(p.getSlug());
            });
            return pi;
        }).collect(Collectors.toList());
        resp.setProjects(projects);

        return resp;
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return "rd_" + HexFormat.of().formatHex(bytes);
    }

    private String sha256(String value) {
        try {
            var md = java.security.MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(value.getBytes()));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
