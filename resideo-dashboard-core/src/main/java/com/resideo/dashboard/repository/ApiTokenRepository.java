package com.resideo.dashboard.repository;

import com.resideo.dashboard.model.entity.ApiToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiTokenRepository extends JpaRepository<ApiToken, UUID> {
    List<ApiToken> findByUserId(UUID userId);
    Optional<ApiToken> findByTokenHash(String tokenHash);
    void deleteByUserId(UUID userId);
}
