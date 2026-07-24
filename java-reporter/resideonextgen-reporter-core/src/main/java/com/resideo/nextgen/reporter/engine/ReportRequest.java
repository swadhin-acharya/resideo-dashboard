package com.resideo.nextgen.reporter.engine;

import java.io.File;

/** Options for a single {@link ReportEngine#generate(ReportRequest)} call. */
public class ReportRequest {
    private File allureResultsDir;
    private File dataDir;
    private String executionId;
    private String executionName;
    private int historyLimit = 50;

    public static ReportRequest create() {
        return new ReportRequest();
    }

    public File getAllureResultsDir() {
        return allureResultsDir;
    }

    public ReportRequest allureResultsDir(File dir) {
        this.allureResultsDir = dir;
        return this;
    }

    public File getDataDir() {
        return dataDir;
    }

    public ReportRequest dataDir(File dataDir) {
        this.dataDir = dataDir;
        return this;
    }

    public String getExecutionId() {
        return executionId;
    }

    public ReportRequest executionId(String executionId) {
        this.executionId = executionId;
        return this;
    }

    public String getExecutionName() {
        return executionName;
    }

    public ReportRequest executionName(String executionName) {
        this.executionName = executionName;
        return this;
    }

    public int getHistoryLimit() {
        return historyLimit;
    }

    public ReportRequest historyLimit(int historyLimit) {
        this.historyLimit = historyLimit;
        return this;
    }
}
