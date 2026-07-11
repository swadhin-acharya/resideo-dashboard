package com.resideo.dashboard.model.dto;

public class ExecutionSummary {

    private long totalExecutions;
    private long passed;
    private long failed;
    private long aborted;
    private long running;
    private double passRate;
    private long totalScenarios;
    private long totalPassed;
    private long totalFailed;
    private long totalSkipped;

    public long getTotalExecutions() { return totalExecutions; }
    public void setTotalExecutions(long totalExecutions) { this.totalExecutions = totalExecutions; }
    public long getPassed() { return passed; }
    public void setPassed(long passed) { this.passed = passed; }
    public long getFailed() { return failed; }
    public void setFailed(long failed) { this.failed = failed; }
    public long getAborted() { return aborted; }
    public void setAborted(long aborted) { this.aborted = aborted; }
    public long getRunning() { return running; }
    public void setRunning(long running) { this.running = running; }
    public double getPassRate() { return passRate; }
    public void setPassRate(double passRate) { this.passRate = passRate; }
    public long getTotalScenarios() { return totalScenarios; }
    public void setTotalScenarios(long totalScenarios) { this.totalScenarios = totalScenarios; }
    public long getTotalPassed() { return totalPassed; }
    public void setTotalPassed(long totalPassed) { this.totalPassed = totalPassed; }
    public long getTotalFailed() { return totalFailed; }
    public void setTotalFailed(long totalFailed) { this.totalFailed = totalFailed; }
    public long getTotalSkipped() { return totalSkipped; }
    public void setTotalSkipped(long totalSkipped) { this.totalSkipped = totalSkipped; }
}
