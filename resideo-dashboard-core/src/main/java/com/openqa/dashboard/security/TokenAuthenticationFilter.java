package com.openqa.dashboard.security;

import com.openqa.dashboard.model.entity.ApiToken;
import com.openqa.dashboard.model.entity.UserEntity;
import com.openqa.dashboard.repository.ApiTokenRepository;
import com.openqa.dashboard.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final ApiTokenRepository apiTokenRepository;
    private final UserRepository userRepository;

    public TokenAuthenticationFilter(ApiTokenRepository apiTokenRepository, UserRepository userRepository) {
        this.apiTokenRepository = apiTokenRepository;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length()).trim();
            String tokenHash = sha256(token);

            apiTokenRepository.findByTokenHash(tokenHash).ifPresent(apiToken -> {
                if (apiToken.getEnabled() && !isExpired(apiToken)) {
                    userRepository.findById(apiToken.getUserId()).ifPresent(user -> {
                        DashboardUserDetails userDetails = new DashboardUserDetails(user);
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        auth.setDetails(apiToken.getProjectId());
                        SecurityContextHolder.getContext().setAuthentication(auth);

                        apiToken.setLastUsedAt(Instant.now());
                        apiTokenRepository.save(apiToken);
                    });
                }
            });
        }

        filterChain.doFilter(request, response);
    }

    private boolean isExpired(ApiToken apiToken) {
        return apiToken.getExpiresAt() != null && apiToken.getExpiresAt().isBefore(Instant.now());
    }

    private String sha256(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(value.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
