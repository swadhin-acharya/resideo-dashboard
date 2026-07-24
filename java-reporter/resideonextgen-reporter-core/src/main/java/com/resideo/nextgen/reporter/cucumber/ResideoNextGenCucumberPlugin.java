package com.resideo.nextgen.reporter.cucumber;

import com.resideo.nextgen.reporter.autoserve.AutoServeSupport;
import com.resideo.nextgen.reporter.engine.ReportEngine;
import com.resideo.nextgen.reporter.engine.ReportRequest;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestRunFinished;

import java.io.File;

/**
 * Register as a Cucumber plugin -- e.g. via
 * {@code @CucumberOptions(plugin = {"com.resideo.nextgen.reporter.cucumber.ResideoNextGenCucumberPlugin"})},
 * or (recommended, zero code change) by setting
 * {@code cucumber.plugin=com.resideo.nextgen.reporter.cucumber.ResideoNextGenCucumberPlugin}
 * in {@code cucumber.properties} / {@code junit-platform.properties} -- to
 * process this run's Allure results into ResideoNextGen dashboard data as
 * soon as the run finishes, and optionally serve it immediately if opted in.
 *
 * Requires an Allure adapter for Cucumber (e.g.
 * io.qameta.allure:allure-cucumber7-jvm) to already be configured -- this
 * plugin does not capture results itself, it only triggers processing of
 * what Allure already wrote. It never fails the actual test run if report
 * generation itself has a problem.
 */
public class ResideoNextGenCucumberPlugin implements EventListener {

    public static final String PROP_ALLURE_RESULTS_DIR = "resideonextgen.allureResultsDir";
    public static final String PROP_OUTPUT_DIR = "resideonextgen.outputDir";
    public static final String PROP_HISTORY_LIMIT = "resideonextgen.historyLimit";

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestRunFinished.class, event -> onTestRunFinished());
    }

    private void onTestRunFinished() {
        File allureResultsDir = resolveAllureResultsDir();
        File dataDir = resolveDataDir();
        int historyLimit = Integer.getInteger(PROP_HISTORY_LIMIT, 50);

        try {
            ReportRequest request = ReportRequest.create()
                    .allureResultsDir(allureResultsDir)
                    .dataDir(dataDir)
                    .executionName("Cucumber Suite")
                    .historyLimit(historyLimit);

            ReportEngine.generate(request);
            AutoServeSupport.maybeAutoServe(dataDir);
        } catch (Exception e) {
            System.err.println("[ResideoNextGen] Failed to generate dashboard data: " + e.getMessage());
        }
    }

    private File resolveAllureResultsDir() {
        String override = System.getProperty(PROP_ALLURE_RESULTS_DIR);
        return new File(override != null ? override : "target/allure-results");
    }

    private File resolveDataDir() {
        String outputDir = System.getProperty(PROP_OUTPUT_DIR, "target/resideonextgen");
        return new File(outputDir, "data");
    }
}
