package com.resideo.nextgen.reporter.model;

public class FailureSummary {
    private String testId;
    private String testName;
    private String feature;
    private int occurrences;
    private String lastSeenExecutionId;
    private long lastSeenAt;

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public int getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(int occurrences) {
        this.occurrences = occurrences;
    }

    public String getLastSeenExecutionId() {
        return lastSeenExecutionId;
    }

    public void setLastSeenExecutionId(String lastSeenExecutionId) {
        this.lastSeenExecutionId = lastSeenExecutionId;
    }

    public long getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(long lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }
}
