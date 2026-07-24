package com.openqa.dashboard.repository;

import com.openqa.dashboard.model.entity.Settings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettingsRepository extends JpaRepository<Settings, Long> {
    Optional<Settings> findByKey(String key);
}
