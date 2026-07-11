package com.resideo.dashboard.controller;

import com.resideo.dashboard.model.entity.Thermostat;
import com.resideo.dashboard.repository.ThermostatRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/thermostats")
public class ThermostatController {

    private final ThermostatRepository thermostatRepository;

    public ThermostatController(ThermostatRepository thermostatRepository) {
        this.thermostatRepository = thermostatRepository;
    }

    @GetMapping
    public ResponseEntity<List<Thermostat>> getAll() {
        return ResponseEntity.ok(thermostatRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Thermostat> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(thermostatRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Thermostat not found: " + id)));
    }

    @PostMapping
    public ResponseEntity<Thermostat> create(@RequestBody Thermostat thermostat) {
        return ResponseEntity.status(HttpStatus.CREATED).body(thermostatRepository.save(thermostat));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Thermostat> update(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        Thermostat t = thermostatRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Thermostat not found: " + id));
        if (body.containsKey("name")) t.setName(body.get("name"));
        if (body.containsKey("serialPort")) t.setSerialPort(body.get("serialPort"));
        if (body.containsKey("firmwareVersion")) t.setFirmwareVersion(body.get("firmwareVersion"));
        if (body.containsKey("status")) t.setStatus(body.get("status"));
        if (body.containsKey("reservedBy")) t.setReservedBy(body.get("reservedBy"));
        return ResponseEntity.ok(thermostatRepository.save(t));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        thermostatRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
