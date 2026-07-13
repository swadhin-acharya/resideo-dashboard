package com.openqa.dashboard.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openqa.dashboard.config.OpenQAProperties;
import com.openqa.dashboard.model.config.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConfigFileService {

    private static final Logger log = LoggerFactory.getLogger(ConfigFileService.class);
    private static final Set<String> CONFIG_EXTENSIONS = Set.of(".properties", ".yml", ".yaml", ".json", ".env");

    private final OpenQAProperties properties;
    private final ObjectMapper jsonMapper;
    private final Yaml yaml;

    public ConfigFileService(OpenQAProperties properties) {
        this.properties = properties;
        this.jsonMapper = new ObjectMapper();
        this.yaml = new Yaml();
    }

    public List<ConfigFileInfo> findConfigFiles() {
        List<ConfigFileInfo> files = new ArrayList<>();
        Path workspace = Paths.get(properties.getWorkspace());
        if (!Files.exists(workspace)) return files;

        try {
            Files.walk(workspace, 3)
                .filter(p -> Files.isRegularFile(p))
                .filter(p -> {
                    String name = p.getFileName().toString().toLowerCase();
                    return CONFIG_EXTENSIONS.stream().anyMatch(name::endsWith);
                })
                .forEach(p -> {
                    try {
                        String fileName = p.getFileName().toString();
                        String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.')) : "";
                        ConfigFileInfo info = new ConfigFileInfo(
                            workspace.relativize(p).toString(),
                            fileName,
                            ext,
                            Files.size(p)
                        );
                        files.add(info);
                    } catch (IOException ignored) {}
                });
        } catch (IOException e) {
            log.warn("Failed to scan config files: {}", e.getMessage());
        }
        return files;
    }

    public MergedConfig parseAndMerge(String selectedPath) {
        MergedConfig merged = new MergedConfig();
        merged.setSelectedFilePath(selectedPath);

        Path workspace = Paths.get(properties.getWorkspace());
        Path filePath = workspace.resolve(selectedPath);

        if (!Files.exists(filePath)) {
            merged.setGroups(List.of());
            merged.setAdditionalConfig(Map.of());
            merged.setCommandTemplate("mvn test {params}");
            return merged;
        }

        Map<String, String> parsed = parseFile(filePath);
        if (parsed == null) parsed = new LinkedHashMap<>();

        DashboardConfig dashConfig = loadDashboardConfig();

        if (dashConfig != null) {
            merged.setCommandTemplate(dashConfig.getCommandTemplate() != null
                ? dashConfig.getCommandTemplate() : "mvn test {params}");

            List<MergedConfig.ParameterGroupView> groups = new ArrayList<>();
            if (dashConfig.getParameters() != null) {
                for (DashboardConfig.ParameterGroup pg : dashConfig.getParameters()) {
                    MergedConfig.ParameterGroupView gv = new MergedConfig.ParameterGroupView();
                    gv.setGroup(pg.getGroup());
                    List<MergedConfig.FieldValue> fieldValues = new ArrayList<>();
                    for (DashboardConfig.FieldDefinition fd : pg.getFields()) {
                        MergedConfig.FieldValue fv = new MergedConfig.FieldValue();
                        fv.setName(fd.getName());
                        fv.setLabel(fd.getLabel());
                        fv.setType(fd.getType());
                        fv.setDefaultValue(fd.getDefaultValue());
                        fv.setPlaceholder(fd.getPlaceholder());
                        fv.setOptions(fd.getOptions());
                        fv.setRequired(fd.getRequired());
                        fv.setMin(fd.getMin());
                        fv.setMax(fd.getMax());
                        fv.setMap(fd.getMap());
                        fv.setMapIfTrue(fd.getMapIfTrue());

                        String lookupKey = fd.getConfigKey() != null ? fd.getConfigKey() : fd.getName();
                        String configValue = findValue(parsed, lookupKey);
                        if (configValue != null) {
                            fv.setValue(configValue);
                            fv.setSource(filePath.getFileName().toString());
                        } else {
                            fv.setValue(fd.getDefaultValue());
                        }
                        fieldValues.add(fv);
                    }
                    gv.setFields(fieldValues);
                    groups.add(gv);
                }
            }
            merged.setGroups(groups);

            Set<String> mappedKeys = new HashSet<>();
            if (dashConfig.getParameters() != null) {
                for (DashboardConfig.ParameterGroup pg : dashConfig.getParameters()) {
                    if (pg.getFields() != null) {
                        for (DashboardConfig.FieldDefinition fd : pg.getFields()) {
                            String lookupKey = fd.getConfigKey() != null ? fd.getConfigKey() : fd.getName();
                            mappedKeys.add(lookupKey);
                            mappedKeys.add(lookupKey.replace(".", "_"));
                            mappedKeys.add(lookupKey.replace("_", "."));
                        }
                    }
                }
            }

            Map<String, String> additional = new LinkedHashMap<>();
            for (Map.Entry<String, String> entry : parsed.entrySet()) {
                if (!mappedKeys.contains(entry.getKey())) {
                    additional.put(entry.getKey(), entry.getValue());
                }
            }
            merged.setAdditionalConfig(additional);

        } else {
            merged.setGroups(List.of());
            merged.setAdditionalConfig(parsed);
            merged.setCommandTemplate("mvn test {params}");
        }

        return merged;
    }

    public String buildCommand(String commandTemplate, MergedConfig merged, Map<String, String> paramValues,
                                Map<String, String> additionalValues) {
        StringBuilder cmd = new StringBuilder();

        if (merged.getGroups() != null) {
            for (MergedConfig.ParameterGroupView gv : merged.getGroups()) {
                if (gv.getFields() != null) {
                    for (MergedConfig.FieldValue fv : gv.getFields()) {
                        String value = paramValues.getOrDefault(fv.getName(), fv.getValue());
                        if (value == null || value.isBlank()) continue;
                        if ("boolean".equals(fv.getType())) {
                            if ("true".equalsIgnoreCase(value) && fv.getMapIfTrue() != null) {
                                String part = fv.getMapIfTrue().replace("{value}", value);
                                cmd.append(" ").append(part);
                            }
                        } else {
                            if (fv.getMap() != null) {
                                String part = fv.getMap().replace("{value}", value);
                                cmd.append(" ").append(part);
                            }
                        }
                    }
                }
            }
        }

        if (additionalValues != null) {
            for (Map.Entry<String, String> entry : additionalValues.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isBlank()) {
                    cmd.append(" -D").append(entry.getKey()).append("=").append(entry.getValue());
                }
            }
        }

        String params = cmd.toString().trim();
        return commandTemplate.replace("{params}", params);
    }

    private DashboardConfig loadDashboardConfig() {
        Path workspace = Paths.get(properties.getWorkspace());
        Path configPath = workspace.resolve("dashboard-config.json");
        if (!Files.exists(configPath)) return null;
        try {
            return jsonMapper.readValue(configPath.toFile(), DashboardConfig.class);
        } catch (Exception e) {
            log.warn("Failed to parse dashboard-config.json: {}", e.getMessage());
            return null;
        }
    }

    private Map<String, String> parseFile(Path filePath) {
        String name = filePath.getFileName().toString().toLowerCase();
        try {
            if (name.endsWith(".properties")) return parseProperties(filePath);
            if (name.endsWith(".yml") || name.endsWith(".yaml")) return parseYaml(filePath);
            if (name.endsWith(".json")) return parseJsonFlat(filePath);
            if (name.endsWith(".env")) return parseEnv(filePath);
        } catch (Exception e) {
            log.warn("Failed to parse {}: {}", filePath, e.getMessage());
        }
        return new LinkedHashMap<>();
    }

    private Map<String, String> parseProperties(Path file) throws IOException {
        Map<String, String> map = new LinkedHashMap<>();
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            props.load(in);
        }
        for (String key : props.stringPropertyNames()) {
            map.put(key, props.getProperty(key));
        }
        return map;
    }

    private Map<String, String> parseYaml(Path file) throws IOException {
        Map<String, String> flat = new LinkedHashMap<>();
        try (InputStream in = Files.newInputStream(file)) {
            Object raw = yaml.load(in);
            if (raw instanceof Map) {
                flattenMap("", (Map<String, Object>) raw, flat);
            }
        }
        return flat;
    }

    @SuppressWarnings("unchecked")
    private void flattenMap(String prefix, Map<String, Object> source, Map<String, String> target) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            if (entry.getValue() instanceof Map) {
                flattenMap(key, (Map<String, Object>) entry.getValue(), target);
            } else {
                target.put(key, entry.getValue() != null ? entry.getValue().toString() : "");
            }
        }
    }

    private Map<String, String> parseJsonFlat(Path file) throws IOException {
        Map<String, String> flat = new LinkedHashMap<>();
        Object raw = jsonMapper.readValue(file.toFile(), Object.class);
        if (raw instanceof Map) {
            flattenMap("", (Map<String, Object>) raw, flat);
        }
        return flat;
    }

    private Map<String, String> parseEnv(Path file) throws IOException {
        Map<String, String> map = new LinkedHashMap<>();
        for (String line : Files.readAllLines(file)) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            int eq = line.indexOf('=');
            if (eq > 0) {
                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();
                if ((value.startsWith("\"") && value.endsWith("\"")) ||
                    (value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }
                map.put(key, value);
            }
        }
        return map;
    }

    private String findValue(Map<String, String> parsed, String key) {
        if (parsed.containsKey(key)) return parsed.get(key);
        String dotted = key.replace("_", ".");
        if (parsed.containsKey(dotted)) return parsed.get(dotted);
        String underscored = key.replace(".", "_");
        if (parsed.containsKey(underscored)) return parsed.get(underscored);
        for (Map.Entry<String, String> entry : parsed.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key) ||
                entry.getKey().equalsIgnoreCase(dotted) ||
                entry.getKey().equalsIgnoreCase(underscored)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
