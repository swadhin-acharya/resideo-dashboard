package com.resideo.dashboard.model.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ExecutionRequest {

    private String name;
    private String executionType;
    private String cucumberTags;
    private List<String> featurePaths;
    private List<String> scenarioNames;
    private String platform;
    private String environment;
    private String firmwareVersion;
    private String appVersion;
    private String mavenProfile;
    private Boolean parallel;
    private Integer retryCount;
    private List<UUID> deviceIds;
    private List<UUID> thermostatIds;
    private String jvmParams;
    private String triggeredBy;
    private String branch;
    private String commitHash;
    private String buildNumber;
    private String mavenCommand;
    private String visibility;
    private String machineName;
    private String source;
    private Map<String, String> additionalConfig;

    public String getMavenCommand() { return mavenCommand; }
    public void setMavenCommand(String mavenCommand) { this.mavenCommand = mavenCommand; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getExecutionType() { return executionType; }
    public void setExecutionType(String executionType) { this.executionType = executionType; }
    public String getCucumberTags() { return cucumberTags; }
    public void setCucumberTags(String cucumberTags) { this.cucumberTags = cucumberTags; }
    public List<String> getFeaturePaths() { return featurePaths; }
    public void setFeaturePaths(List<String> featurePaths) { this.featurePaths = featurePaths; }
    public List<String> getScenarioNames() { return scenarioNames; }
    public void setScenarioNames(List<String> scenarioNames) { this.scenarioNames = scenarioNames; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public String getFirmwareVersion() { return firmwareVersion; }
    public void setFirmwareVersion(String firmwareVersion) { this.firmwareVersion = firmwareVersion; }
    public String getAppVersion() { return appVersion; }
    public void setAppVersion(String appVersion) { this.appVersion = appVersion; }
    public String getMavenProfile() { return mavenProfile; }
    public void setMavenProfile(String mavenProfile) { this.mavenProfile = mavenProfile; }
    public Boolean getParallel() { return parallel; }
    public void setParallel(Boolean parallel) { this.parallel = parallel; }
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    public List<UUID> getDeviceIds() { return deviceIds; }
    public void setDeviceIds(List<UUID> deviceIds) { this.deviceIds = deviceIds; }
    public List<UUID> getThermostatIds() { return thermostatIds; }
    public void setThermostatIds(List<UUID> thermostatIds) { this.thermostatIds = thermostatIds; }
    public String getJvmParams() { return jvmParams; }
    public void setJvmParams(String jvmParams) { this.jvmParams = jvmParams; }
    public String getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(String triggeredBy) { this.triggeredBy = triggeredBy; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public String getCommitHash() { return commitHash; }
    public void setCommitHash(String commitHash) { this.commitHash = commitHash; }
    public String getBuildNumber() { return buildNumber; }
    public void setBuildNumber(String buildNumber) { this.buildNumber = buildNumber; }
    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }
    public String getMachineName() { return machineName; }
    public void setMachineName(String machineName) { this.machineName = machineName; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Map<String, String> getAdditionalConfig() { return additionalConfig; }
    public void setAdditionalConfig(Map<String, String> additionalConfig) { this.additionalConfig = additionalConfig; }
}
