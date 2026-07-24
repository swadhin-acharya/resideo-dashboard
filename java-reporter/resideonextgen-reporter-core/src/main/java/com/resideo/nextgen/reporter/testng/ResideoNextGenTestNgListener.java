package com.resideo.nextgen.reporter.testng;

import com.resideo.nextgen.reporter.autoserve.AutoServeSupport;
import com.resideo.nextgen.reporter.engine.ReportEngine;
import com.resideo.nextgen.reporter.engine.ReportRequest;
import org.testng.ISuite;
import org.testng.ISuiteListener;

import java.io.File;

/**
 * Attach to a TestNG suite (via {@code testng.xml <listeners>}, a
 * {@code @Listeners} annotation, or automatically -- this jar registers
 * itself as a TestNG ServiceLoader listener, see
 * {@code META-INF/services/org.testng.ITestNGListener}) to process this
 * run's Allure results into ResideoNextGen dashboard data as soon as the
 * suite finishes, and optionally serve it immediately if opted in.
 *
 * Requires an Allure adapter for TestNG (io.qameta.allure:allure-testng) to
 * already be configured -- this listener does not capture results itself,
 * it only triggers processing of what Allure already wrote. It never fails
 * the actual test suite if report generation itself has a problem.
 */
public class ResideoNextGenTestNgListener implements ISuiteListener {

    public static final String PROP_ALLURE_RESULTS_DIR = "resideonextgen.allureResultsDir";
    public static final String PROP_OUTPUT_DIR = "resideonextgen.outputDir";
    public static final String PROP_HISTORY_LIMIT = "resideonextgen.historyLimit";

    @Override
    public void onFinish(ISuite suite) {
        File allureResultsDir = resolveAllureResultsDir();
        File dataDir = resolveDataDir();
        int historyLimit = Integer.getInteger(PROP_HISTORY_LIMIT, 50);

        try {
            ReportRequest request = ReportRequest.create()
                    .allureResultsDir(allureResultsDir)
                    .dataDir(dataDir)
                    .executionName(suite.getName())
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
