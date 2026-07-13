package com.openqa.dashboard.model.config;

import java.util.List;

public class DashboardConfig {

    private String frameworkName;
    private String buildTool;
    private String commandTemplate = "mvn test {params}";
    private List<ParameterGroup> parameters;

    public String getFrameworkName() { return frameworkName; }
    public void setFrameworkName(String frameworkName) { this.frameworkName = frameworkName; }
    public String getBuildTool() { return buildTool; }
    public void setBuildTool(String buildTool) { this.buildTool = buildTool; }
    public String getCommandTemplate() { return commandTemplate; }
    public void setCommandTemplate(String commandTemplate) { this.commandTemplate = commandTemplate; }
    public List<ParameterGroup> getParameters() { return parameters; }
    public void setParameters(List<ParameterGroup> parameters) { this.parameters = parameters; }

    public static class ParameterGroup {
        private String group;
        private List<FieldDefinition> fields;

        public String getGroup() { return group; }
        public void setGroup(String group) { this.group = group; }
        public List<FieldDefinition> getFields() { return fields; }
        public void setFields(List<FieldDefinition> fields) { this.fields = fields; }
    }

    public static class FieldDefinition {
        private String name;
        private String label;
        private String type;
        private String configKey;
        private String defaultValue;
        private String placeholder;
        private List<String> options;
        private Boolean required;
        private Number min;
        private Number max;
        private String map;
        private String mapIfTrue;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getConfigKey() { return configKey; }
        public void setConfigKey(String configKey) { this.configKey = configKey; }
        public String getDefaultValue() { return defaultValue; }
        public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
        public String getPlaceholder() { return placeholder; }
        public void setPlaceholder(String placeholder) { this.placeholder = placeholder; }
        public List<String> getOptions() { return options; }
        public void setOptions(List<String> options) { this.options = options; }
        public Boolean getRequired() { return required; }
        public void setRequired(Boolean required) { this.required = required; }
        public Number getMin() { return min; }
        public void setMin(Number min) { this.min = min; }
        public Number getMax() { return max; }
        public void setMax(Number max) { this.max = max; }
        public String getMap() { return map; }
        public void setMap(String map) { this.map = map; }
        public String getMapIfTrue() { return mapIfTrue; }
        public void setMapIfTrue(String mapIfTrue) { this.mapIfTrue = mapIfTrue; }
    }
}
