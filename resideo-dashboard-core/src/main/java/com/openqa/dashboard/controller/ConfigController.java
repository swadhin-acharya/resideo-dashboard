package com.openqa.dashboard.controller;

import com.openqa.dashboard.model.config.ConfigFileInfo;
import com.openqa.dashboard.model.config.MergedConfig;
import com.openqa.dashboard.service.ConfigFileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/config")
public class ConfigController {

    private final ConfigFileService configFileService;

    public ConfigController(ConfigFileService configFileService) {
        this.configFileService = configFileService;
    }

    @GetMapping("/available-files")
    public ResponseEntity<List<ConfigFileInfo>> getAvailableFiles() {
        return ResponseEntity.ok(configFileService.findConfigFiles());
    }

    @PostMapping("/parse")
    public ResponseEntity<MergedConfig> parse(@RequestBody Map<String, String> body) {
        String filePath = body.get("filePath");
        if (filePath == null || filePath.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(configFileService.parseAndMerge(filePath));
    }

    @PostMapping("/preview")
    public ResponseEntity<Map<String, String>> preview(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        Map<String, String> paramValues = (Map<String, String>) body.get("paramValues");
        @SuppressWarnings("unchecked")
        Map<String, String> additionalValues = (Map<String, String>) body.get("additionalValues");
        String template = (String) body.get("commandTemplate");
        if (template == null) template = "mvn test {params}";

        String selectedFilePath = (String) body.get("selectedFilePath");
        MergedConfig merged;
        if (selectedFilePath != null && !selectedFilePath.isBlank()) {
            merged = configFileService.parseAndMerge(selectedFilePath);
        } else {
            merged = new MergedConfig();
            merged.setGroups(List.of());
            merged.setCommandTemplate(template);
        }
        merged.setAdditionalConfig(additionalValues != null ? additionalValues : Map.of());

        String command = configFileService.buildCommand(template, merged, paramValues, additionalValues);
        return ResponseEntity.ok(Map.of("command", command));
    }
}
