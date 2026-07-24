package com.openqa.dashboard.controller;

import com.openqa.dashboard.model.entity.Device;
import com.openqa.dashboard.service.DeviceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public List<Device> getAll() {
        return deviceService.getAllDevices();
    }

    @GetMapping("/count")
    public Map<String, Long> getCount() {
        return Map.of(
                "online", deviceService.countOnline(),
                "offline", deviceService.countOffline(),
                "total", deviceService.countOnline() + deviceService.countOffline()
        );
    }
}
