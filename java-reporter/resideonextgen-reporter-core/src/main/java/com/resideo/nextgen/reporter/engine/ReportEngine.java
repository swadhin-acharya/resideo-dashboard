package com.resideo.nextgen.reporter.engine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.resideo.nextgen.reporter.allure.AllureResultReader;
import com.resideo.nextgen.reporter.json.JsonViewBuilder;
import com.resideo.nextgen.reporter.model.CategoryRule;
import com.resideo.nextgen.reporter.model.CategorySummary;
import com.resideo.nextgen.reporter.model.EnvironmentInfo;
import com.resideo.nextgen.reporter.model.ExecutionSummary;
import com.resideo.nextgen.reporter.model.ExecutorInfo;
import com.resideo.nextgen.reporter.model.FeatureSummary;
import com.resideo.nextgen.reporter.model.TestResult;
import com.resideo.nextgen.reporter.model.TrendPoint;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

/**
 * Single orchestration entry point: reads an allure-results directory, runs
 * the one calculation layer ({@link Normalizer}), merges history, and
 * writes the canonical ResideoNextGen dashboard JSON. Allure is always the
 * source of truth -- this module never captures or recalculates results of
 * its own; it only requires that your framework's Allure adapter (e.g.
 * io.qameta.allure:allure-testng or io.qameta.allure:allure-cucumber7-jvm)
 * already populated {@code allure-results/}.
 *
 * Called from the TestNG listener's suite-finish hook, the Cucumber
 * plugin's test-run-finish hook, and directly by
 * {@code mvn resideonextgen:serve}.
 *
 * <p>Reruns of the same test (matched by Allure historyId) are collapsed to
 * one canonical result before anything is calculated -- see
 * {@link Normalizer#collapseRetries}. Optionally, an environment.properties
 * {@code suite} value (see {@link AllureResultReader#readReportGroupKey})
 * lets several partial/rerun {@code mvn test} invocations that share the
 * same allure-results directory keep merging into a single report entry
 * until that value changes.
 */
public final class ReportEngine {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private ReportEngine() {
    }

    public static ExecutionSummary generate(ReportRequest request) throws IOException {
        File allureResultsDir = request.getAllureResultsDir();
        if (!AllureResultReader.hasResults(allureResultsDir)) {
            throw new IllegalStateException(
                    "No Allure results found at "
                            + (allureResultsDir != null ? allureResultsDir.getAbsolutePath() : "(none configured)")
                            + ". ResideoNextGen reads Allure's structured results as its single source of "
                            + "truth -- add your framework's Allure adapter (e.g. io.qameta.allure:allure-testng "
                            + "or io.qameta.allure:allure-cucumber7-jvm) so allure-results is populated by "
                            + "`mvn test`, then re-run.");
        }

        List<TestResult> rawResults = AllureResultReader.readResults(allureResultsDir);
        List<TestResult> results = Normalizer.collapseRetries(rawResults);
        EnvironmentInfo environment = AllureResultReader.readEnvironment(allureResultsDir);
        ExecutorInfo executor = AllureResultReader.readExecutor(allureResultsDir);
        List<CategoryRule> categoryRules = AllureResultReader.readCategoryRules(allureResultsDir);
        String reportGroupKey = AllureResultReader.readReportGroupKey(allureResultsDir);

        long suiteStart = earliestStart(results);
        long suiteEnd = latestStop(results);

        // Priority: an explicit executionId always wins; otherwise a
        // "suite" grouping key from environment.properties (see
        // AllureResultReader#readReportGroupKey) lets multiple partial/rerun
        // `mvn test` invocations accumulate into one report entry; otherwise
        // fall back to the executor's buildOrder, then a timestamp.
        String executionId = request.getExecutionId() != null
                ? request.getExecutionId()
                : reportGroupKey != null
                ? reportGroupKey
                : (executor != null && executor.getBuildOrder() != null
                ? executor.getBuildOrder()
                : String.valueOf(Instant.now().toEpochMilli()));

        String executionName = request.getExecutionName() != null
                ? request.getExecutionName()
                : reportGroupKey != null
                ? reportGroupKey
                : (executor != null && executor.getBuildName() != null
                ? executor.getBuildName()
                : "Execution " + executionId);

        ExecutionSummary summary = Normalizer.buildExecutionSummary(
                executionId, executionName, results, suiteStart, suiteEnd, environment, executor);

        List<FeatureSummary> features = Normalizer.buildFeatureSummaries(results);
        List<CategorySummary> categories = Normalizer.buildCategorySummaries(results, categoryRules);

        Path dataDir = request.getDataDir().toPath();

        // mergeExecutions/mergeTrends persist full-detail internal history
        // (dot-prefixed files) and return it so we can compute the
        // vs-previous-run comparison and map it into the leaner public
        // shapes below -- see HistoryStore's class Javadoc for why the
        // internal and public shapes are deliberately different.
        List<ExecutionSummary> executionHistory = HistoryStore.mergeExecutions(dataDir, summary, request.getHistoryLimit());

        TrendPoint trendPoint = new TrendPoint();
        trendPoint.setExecutionId(executionId);
        trendPoint.setTimestamp(suiteStart);
        trendPoint.setPassed(summary.getPassed());
        trendPoint.setFailed(summary.getFailed());
        trendPoint.setBroken(summary.getBroken());
        trendPoint.setSkipped(summary.getSkipped());
        trendPoint.setPassRate(summary.getPassRate());
        trendPoint.setDurationMs(summary.getDurationMs());
        List<TrendPoint> trendHistory = HistoryStore.mergeTrends(dataDir, trendPoint, request.getHistoryLimit());

        List<com.resideo.nextgen.reporter.model.FailureSummary> failureHistory =
                HistoryStore.mergeFailures(dataDir, executionId, Normalizer.failuresOnly(results), suiteStart);

        JsonObject comparison = ComparisonEngine.compute(executionHistory, summary);

        DashboardDataWriter.writeJson(dataDir, "summary.json",
                JsonViewBuilder.summary(executionId, summary, comparison, environment, executor));
        DashboardDataWriter.writeJson(dataDir, "executions.json", JsonViewBuilder.recentExecutionRows(executionHistory));
        DashboardDataWriter.writeJson(dataDir, "trends.json", JsonViewBuilder.trendPoints(trendHistory));
        DashboardDataWriter.writeJson(dataDir, "features.json", JsonViewBuilder.featureSummaries(features));
        DashboardDataWriter.writeJson(dataDir, "failures.json", JsonViewBuilder.failureSummaries(failureHistory));
        DashboardDataWriter.writeJson(dataDir, "categories.json", JsonViewBuilder.categorySummaries(categories));
        DashboardDataWriter.writeJson(dataDir, "environment.json", JsonViewBuilder.environmentInfo(environment));
        // tests.json is not yet fetched by any shipped page (no Test Details
        // page exists today) -- kept in the internal shape rather than
        // spending effort matching a canonical shape nothing reads yet.
        DashboardDataWriter.writeJson(dataDir, "tests.json", GSON.toJsonTree(results));

        return summary;
    }

    private static long earliestStart(List<TestResult> results) {
        return results.stream()
                .mapToLong(TestResult::getStartTime)
                .filter(t -> t > 0)
                .min()
                .orElse(Instant.now().toEpochMilli());
    }

    private static long latestStop(List<TestResult> results) {
        return results.stream()
                .mapToLong(TestResult::getStopTime)
                .filter(t -> t > 0)
                .max()
                .orElse(Instant.now().toEpochMilli());
    }
}
