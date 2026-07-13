package com.openqa.dashboard.controller;

import com.openqa.dashboard.model.entity.Schedule;
import com.openqa.dashboard.repository.ScheduleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schedules")
public class ScheduleController {

    private final ScheduleRepository scheduleRepository;

    public ScheduleController(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    @GetMapping
    public ResponseEntity<List<Schedule>> getAll() {
        return ResponseEntity.ok(scheduleRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Schedule> create(@RequestBody Schedule schedule) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleRepository.save(schedule));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Schedule> update(@PathVariable UUID id, @RequestBody Schedule updated) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Schedule not found: " + id));
        if (updated.getName() != null) schedule.setName(updated.getName());
        if (updated.getCronExpression() != null) schedule.setCronExpression(updated.getCronExpression());
        if (updated.getExecutionConfig() != null) schedule.setExecutionConfig(updated.getExecutionConfig());
        if (updated.getEnabled() != null) schedule.setEnabled(updated.getEnabled());
        return ResponseEntity.ok(scheduleRepository.save(schedule));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        scheduleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
