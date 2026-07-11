package com.resideo.dashboard.model.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MergedConfig {

    private List<ParameterGroupView> groups;
    private Map<String, String> additionalConfig = new LinkedHashMap<>();
    private String commandTemplate;
    private String selectedFilePath;

    public List<ParameterGroupView> getGroups() { return groups; }
    public void setGroups(List<ParameterGroupView> groups) { this.groups = groups; }
    public Map<String, String> getAdditionalConfig() { return additionalConfig; }
    public void setAdditionalConfig(Map<String, String> additionalConfig) { this.additionalConfig = additionalConfig; }
    public String getCommandTemplate() { return commandTemplate; }
    public void setCommandTemplate(String commandTemplate) { this.commandTemplate = commandTemplate; }
    public String getSelectedFilePath() { return selectedFilePath; }
    public void setSelectedFilePath(String selectedFilePath) { this.selectedFilePath = selectedFilePath; }

    public static class ParameterGroupView {
        private String group;
        private List<FieldValue> fields;

        public String getGroup() { return group; }
        public void setGroup(String group) { this.group = group; }
        public List<FieldValue> getFields() { return fields; }
        public void setFields(List<FieldValue> fields) { this.fields = fields; }
    }

    public static class FieldValue {
        private String name;
        private String label;
        private String type;
        private String value;
        private String defaultValue;
        private String placeholder;
        private List<String> options;
        private Boolean required;
        private Number min;
        private Number max;
        private String map;
        private String mapIfTrue;
        private String source;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
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
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
    }
}
