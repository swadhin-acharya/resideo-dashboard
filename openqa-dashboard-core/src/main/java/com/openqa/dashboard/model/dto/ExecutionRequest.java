package com.openqa.dashboard.model.dto;

public class ExecutionRequest {
    private String name;
    private String executionName;
    private String framework;
    private String branch;
    private String machine;
    private String platform;
    private String environment;
    private String buildNumber;
    private String executionType;
    private String type;
    private String source;
    private String mavenCommand;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getExecutionName() { return executionName; }
    public void setExecutionName(String executionName) { this.executionName = executionName; }
    public String getFramework() { return framework; }
    public void setFramework(String framework) { this.framework = framework; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public String getMachine() { return machine; }
    public void setMachine(String machine) { this.machine = machine; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public String getBuildNumber() { return buildNumber; }
    public void setBuildNumber(String buildNumber) { this.buildNumber = buildNumber; }
    public String getExecutionType() { return executionType; }
    public void setExecutionType(String executionType) { this.executionType = executionType; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getMavenCommand() { return mavenCommand; }
    public void setMavenCommand(String mavenCommand) { this.mavenCommand = mavenCommand; }
}
