package com.resideo.dashboard.model.config;

public class ConfigFileInfo {

    private String path;
    private String fileName;
    private String extension;
    private long size;
    private int keyCount;

    public ConfigFileInfo() {}

    public ConfigFileInfo(String path, String fileName, String extension, long size) {
        this.path = path;
        this.fileName = fileName;
        this.extension = extension;
        this.size = size;
    }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getExtension() { return extension; }
    public void setExtension(String extension) { this.extension = extension; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    public int getKeyCount() { return keyCount; }
    public void setKeyCount(int keyCount) { this.keyCount = keyCount; }
}
