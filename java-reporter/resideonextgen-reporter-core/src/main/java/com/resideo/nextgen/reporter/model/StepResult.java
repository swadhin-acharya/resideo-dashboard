package com.resideo.nextgen.reporter.model;

import java.util.ArrayList;
import java.util.List;

public class StepResult {
    private String name;
    private ResultStatus status;
    private long durationMs;
    private List<StepResult> steps = new ArrayList<>();

    public StepResult() {
    }

    public StepResult(String name, ResultStatus status, long durationMs) {
        this.name = name;
        this.status = status;
        this.durationMs = durationMs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ResultStatus getStatus() {
        return status;
    }

    public void setStatus(ResultStatus status) {
        this.status = status;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public List<StepResult> getSteps() {
        return steps;
    }

    public void setSteps(List<StepResult> steps) {
        this.steps = steps;
    }
}
