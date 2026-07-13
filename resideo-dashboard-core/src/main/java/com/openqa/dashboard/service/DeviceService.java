package com.openqa.dashboard.service;

import com.openqa.dashboard.model.dto.DeviceRequest;
import com.openqa.dashboard.model.entity.Device;
import com.openqa.dashboard.model.enums.DeviceStatus;
import com.openqa.dashboard.model.enums.Platform;
import com.openqa.dashboard.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Transactional(readOnly = true)
    public List<Device> getAll() {
        return deviceRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Device> getAllByProject(UUID projectId) {
        if (projectId != null) {
            return deviceRepository.findByProjectId(projectId);
        }
        return deviceRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Device getById(UUID id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Device not found: " + id));
    }

    public Device create(DeviceRequest request) {
        Device device = new Device();
        device.setName(request.getName());
        device.setPlatform(Platform.valueOf(request.getPlatform().toUpperCase()));
        device.setUdid(request.getUdid());
        device.setOsVersion(request.getOsVersion());
        device.setCloudProvider(request.getCloudProvider());
        device.setStatus(DeviceStatus.AVAILABLE);
        if (request.getProjectId() != null) {
            device.setProjectId(request.getProjectId());
        }
        return deviceRepository.save(device);
    }

    public Device update(UUID id, DeviceRequest request) {
        Device device = getById(id);
        if (request.getName() != null) device.setName(request.getName());
        if (request.getPlatform() != null) device.setPlatform(Platform.valueOf(request.getPlatform().toUpperCase()));
        if (request.getUdid() != null) device.setUdid(request.getUdid());
        if (request.getOsVersion() != null) device.setOsVersion(request.getOsVersion());
        if (request.getCloudProvider() != null) device.setCloudProvider(request.getCloudProvider());
        if (request.getStatus() != null) device.setStatus(DeviceStatus.valueOf(request.getStatus().toUpperCase()));
        return deviceRepository.save(device);
    }

    public Device reserve(UUID id, String reservedBy) {
        Device device = getById(id);
        device.setStatus(DeviceStatus.RESERVED);
        device.setReservedBy(reservedBy);
        device.setReservedUntil(Instant.now().plusSeconds(3600));
        return deviceRepository.save(device);
    }

    public Device release(UUID id) {
        Device device = getById(id);
        device.setStatus(DeviceStatus.AVAILABLE);
        device.setReservedBy(null);
        device.setReservedUntil(null);
        return deviceRepository.save(device);
    }

    public void delete(UUID id) {
        deviceRepository.deleteById(id);
    }
}
