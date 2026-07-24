package com.openqa.dashboard.repository;

import com.openqa.dashboard.model.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, String> {
    Optional<Device> findByDeviceId(String deviceId);
    long countByStatus(String status);
}
