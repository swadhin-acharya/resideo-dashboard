package com.openqa.dashboard.controller;

import com.openqa.dashboard.model.dto.DeviceRequest;
import com.openqa.dashboard.model.entity.Device;
import com.openqa.dashboard.service.DeviceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public ResponseEntity<List<Device>> getAll(
            @RequestHeader(value = "X-Project-Id", required = false) String projectIdStr) {
        UUID projectId = projectIdStr != null ? UUID.fromString(projectIdStr) : null;
        return ResponseEntity.ok(deviceService.getAllByProject(projectId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Device> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(deviceService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Device> create(@RequestBody DeviceRequest request,
                                          @RequestHeader(value = "X-Project-Id", required = false) String projectIdStr) {
        if (projectIdStr != null && request.getProjectId() == null) {
            request.setProjectId(UUID.fromString(projectIdStr));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceService.create(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Device> update(@PathVariable UUID id, @RequestBody DeviceRequest request) {
        return ResponseEntity.ok(deviceService.update(id, request));
    }

    @PostMapping("/{id}/reserve")
    public ResponseEntity<Device> reserve(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(deviceService.reserve(id, body.getOrDefault("reservedBy", "anonymous")));
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<Device> release(@PathVariable UUID id) {
        return ResponseEntity.ok(deviceService.release(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deviceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
