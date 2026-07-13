package com.openqa.dashboard.repository;

import com.openqa.dashboard.model.entity.Device;
import com.openqa.dashboard.model.enums.DeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {

    List<Device> findByStatus(DeviceStatus status);

    List<Device> findByPlatform(String platform);

    long countByStatus(DeviceStatus status);

    List<Device> findByProjectId(UUID projectId);

    List<Device> findByProjectIdAndStatus(UUID projectId, DeviceStatus status);
}
