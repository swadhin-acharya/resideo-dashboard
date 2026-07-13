package com.openqa.dashboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openqa")
public class OpenQAProperties {

    private boolean enabled = true;
    private String workspace = ".";
    private String dbMode = "FILE";
    private long pollIntervalMs = 5000;
    private String cucumberJsonPath = "target/cucumber.json";
    private String reportPath = "target/open-reporter/report.html";
    private String configPath = "dashboard-config.json";

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getWorkspace() { return workspace; }
    public void setWorkspace(String workspace) { this.workspace = workspace; }
    public String getDbMode() { return dbMode; }
    public void setDbMode(String dbMode) { this.dbMode = dbMode; }
    public long getPollIntervalMs() { return pollIntervalMs; }
    public void setPollIntervalMs(long pollIntervalMs) { this.pollIntervalMs = pollIntervalMs; }
    public String getCucumberJsonPath() { return cucumberJsonPath; }
    public void setCucumberJsonPath(String cucumberJsonPath) { this.cucumberJsonPath = cucumberJsonPath; }
    public String getReportPath() { return reportPath; }
    public void setReportPath(String reportPath) { this.reportPath = reportPath; }
    public String getConfigPath() { return configPath; }
    public void setConfigPath(String configPath) { this.configPath = configPath; }
}
