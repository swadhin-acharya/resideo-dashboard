package com.resideo.nextgen.reporter.model;

import java.util.ArrayList;
import java.util.List;

/** A custom failure-categorization rule, read from Allure's categories.json. */
public class CategoryRule {
    private String name;
    private List<String> matchedStatuses = new ArrayList<>();
    private String messageRegex;
    private String traceRegex;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getMatchedStatuses() {
        return matchedStatuses;
    }

    public void setMatchedStatuses(List<String> matchedStatuses) {
        this.matchedStatuses = matchedStatuses;
    }

    public String getMessageRegex() {
        return messageRegex;
    }

    public void setMessageRegex(String messageRegex) {
        this.messageRegex = messageRegex;
    }

    public String getTraceRegex() {
        return traceRegex;
    }

    public void setTraceRegex(String traceRegex) {
        this.traceRegex = traceRegex;
    }
}
