package com.resideo.dashboard.model.dto;

import java.util.UUID;

public class DeviceRequest {

    private String name;
    private String platform;
    private String udid;
    private String osVersion;
    private String cloudProvider;
    private String status;
    private String reservedBy;
    private UUID projectId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getUdid() { return udid; }
    public void setUdid(String udid) { this.udid = udid; }
    public String getOsVersion() { return osVersion; }
    public void setOsVersion(String osVersion) { this.osVersion = osVersion; }
    public String getCloudProvider() { return cloudProvider; }
    public void setCloudProvider(String cloudProvider) { this.cloudProvider = cloudProvider; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReservedBy() { return reservedBy; }
    public void setReservedBy(String reservedBy) { this.reservedBy = reservedBy; }
    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }
}
