package com.openqa.dashboard.service;

import com.openqa.dashboard.model.entity.Device;
import com.openqa.dashboard.repository.DeviceRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class DeviceService {

    private static final Logger log = LoggerFactory.getLogger(DeviceService.class);

    private final DeviceRepository deviceRepository;
    private ScheduledExecutorService scheduler;
    private boolean adbAvailable = false;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @PostConstruct
    public void start() {
        adbAvailable = checkAdb();
        if (!adbAvailable) {
            log.info("ADB not found - device detection disabled");
            return;
        }
        log.info("ADB detected - starting device monitor");
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::scanDevices, 0, 15, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void stop() {
        if (scheduler != null) scheduler.shutdown();
    }

    private boolean checkAdb() {
        try {
            Process p = Runtime.getRuntime().exec("adb version");
            p.waitFor(3, TimeUnit.SECONDS);
            return p.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private void scanDevices() {
        try {
            Process p = Runtime.getRuntime().exec("adb devices");
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            boolean headerPassed = false;
            while ((line = reader.readLine()) != null) {
                if (!headerPassed) { headerPassed = true; continue; }
                line = line.trim();
                if (line.isEmpty() || line.startsWith("*") || line.startsWith("daemon")) continue;
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    String deviceId = parts[0];
                    String adbStatus = parts[1];
                    Device device = deviceRepository.findByDeviceId(deviceId)
                            .orElseGet(() -> {
                                Device d = new Device();
                                d.setDeviceId(deviceId);
                                return d;
                            });
                    device.setStatus("device".equals(adbStatus) ? "ONLINE" : "OFFLINE");
                    device.setLastSeen(Instant.now().toString());
                    device.setPlatform("ANDROID");

                    if ("device".equals(adbStatus) && (device.getBrand() == null || device.getModel() == null)) {
                        populateDeviceInfo(device, deviceId);
                    }

                    deviceRepository.save(device);
                }
            }
            markMissingOffline();
        } catch (Exception e) {
            log.warn("Device scan failed: {}", e.getMessage());
        }
    }

    private void populateDeviceInfo(Device device, String deviceId) {
        try {
            Process p = Runtime.getRuntime().exec("adb -s " + deviceId + " shell getprop ro.product.brand");
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String brand = reader.readLine();
            if (brand != null && !brand.isBlank()) device.setBrand(brand.trim());

            p = Runtime.getRuntime().exec("adb -s " + deviceId + " shell getprop ro.product.model");
            reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String model = reader.readLine();
            if (model != null && !model.isBlank()) device.setModel(model.trim());

            p = Runtime.getRuntime().exec("adb -s " + deviceId + " shell getprop ro.build.version.release");
            reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String osVer = reader.readLine();
            if (osVer != null && !osVer.isBlank()) device.setOsVersion(osVer.trim());
        } catch (Exception e) {
            log.warn("Failed to get device info for {}: {}", deviceId, e.getMessage());
        }
    }

    private void markMissingOffline() {
        long threshold = Instant.now().minusSeconds(30).getEpochSecond();
        deviceRepository.findAll().forEach(d -> {
            if (d.getLastSeen() != null) {
                try {
                    long lastSeen = Instant.parse(d.getLastSeen()).getEpochSecond();
                    if (lastSeen < threshold) {
                        d.setStatus("OFFLINE");
                        deviceRepository.save(d);
                    }
                } catch (Exception ignored) {}
            }
        });
    }

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public long countOnline() {
        return deviceRepository.countByStatus("ONLINE");
    }

    public long countOffline() {
        return deviceRepository.countByStatus("OFFLINE");
    }
}
