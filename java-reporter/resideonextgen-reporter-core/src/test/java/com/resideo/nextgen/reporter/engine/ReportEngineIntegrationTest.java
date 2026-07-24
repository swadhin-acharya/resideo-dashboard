package com.resideo.nextgen.reporter.engine;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end tests for {@link ReportEngine} against a real (small, hand
 * -written) allure-results directory on disk -- no Selenium, no browser,
 * no Maven Central access needed, just {@code mvn test} in this module.
 *
 * <p>The main scenario here is exactly the one from the project's real
 * workflow: run a subset of tests, run another subset, rerun a previously
 * failed test, all writing into the *same* allure-results directory (as
 * `mvn test` without `mvn clean` naturally does) under the same
 * environment.properties {@code suite} value -- and confirm it all lands
 * in one ResideoNextGen report entry, with reruns superseding earlier
 * failed attempts rather than being double-counted.
 */
class ReportEngineIntegrationTest {

    @Test
    void requiresAllureResultsToBePresent(@TempDir Path allureDir, @TempDir Path dataDir) {
        ReportRequest request = ReportRequest.create()
                .allureResultsDir(allureDir.resolve("missing").toFile())
                .dataDir(dataDir.toFile());

        assertThrows(IllegalStateException.class, () -> ReportEngine.generate(request));
    }

    @Test
    void partialRunsUnderTheSameSuiteMergeIntoOneReport(@TempDir Path allureDir, @TempDir Path dataDir) throws IOException {
        // --- Stage A: first partial run (2 passed, 1 failed) under suite v1 ---
        writeResult(allureDir, "1-result.json", "hist-1", "Verify Login", "passed", 1_000, 2_000, null);
        writeResult(allureDir, "2-result.json", "hist-2", "Verify Logout", "passed", 2_000, 2_500, null);
        writeResult(allureDir, "3-result.json", "hist-3", "Verify Checkout Total", "failed", 2_500, 3_000,
                "expected 42.00 but was 41.50");
        writeEnvironment(allureDir, "Denali_Regression_v1");

        ReportRequest request = ReportRequest.create()
                .allureResultsDir(allureDir.toFile())
                .dataDir(dataDir.toFile())
                .historyLimit(50);

        ReportEngine.generate(request);

        JsonArray executionsAfterStageA = readJsonArray(dataDir.resolve("executions.json"));
        assertEquals(1, executionsAfterStageA.size());
        JsonObject execA = executionsAfterStageA.get(0).getAsJsonObject();
        assertEquals("Denali_Regression_v1", execA.get("executionId").getAsString());
        assertEquals(3, execA.get("total").getAsInt());
        assertEquals(1, execA.get("failed").getAsInt());
        // Regression guard for the real bug this locks in: the frontend's
        // RecentExecutionRow uses "duration" and "date" (ISO string), and a
        // lowercase status string -- not "durationMs"/"timestamp"/"FAILED".
        assertEquals("failed", execA.get("status").getAsString());
        assertNotNull(execA.get("duration"));
        assertTrue(execA.get("date").getAsString().contains("T"), "expected an ISO-8601 date string");

        JsonObject summaryAfterStageA = readJsonObject(dataDir.resolve("summary.json"));
        assertNotNull(summaryAfterStageA.get("generatedAt"));
        JsonObject comparisonAfterStageA = summaryAfterStageA.getAsJsonObject("comparison");
        // Regression guard for the real bug this locks in: "comparison" must
        // always be present (KpiCardsRow crashed the whole Overview page
        // reading comparison.total.available when this key was missing
        // entirely) -- and marked unavailable, never fabricated, when
        // there's no earlier execution yet.
        assertNotNull(comparisonAfterStageA);
        assertFalse(comparisonAfterStageA.getAsJsonObject("total").get("available").getAsBoolean());

        // --- Stage B: second partial run, same allure-results dir, same suite:
        //     a brand-new test, plus a rerun of the earlier failure that now
        //     passes. `mvn test` without `mvn clean` naturally accumulates
        //     files like this. ---
        writeResult(allureDir, "3-retry-result.json", "hist-3", "Verify Checkout Total", "passed", 5_000, 5_500, null);
        writeResult(allureDir, "4-result.json", "hist-4", "Verify Cart Badge", "passed", 5_500, 6_000, null);

        ReportEngine.generate(request);

        JsonArray executionsAfterStageB = readJsonArray(dataDir.resolve("executions.json"));
        // Still one report entry -- the second processing pass replaced the
        // first, it did not add a second entry.
        assertEquals(1, executionsAfterStageB.size());
        JsonObject execB = executionsAfterStageB.get(0).getAsJsonObject();
        assertEquals("Denali_Regression_v1", execB.get("executionId").getAsString());
        // 4 distinct tests (not 5): the rerun collapsed onto hist-3 instead
        // of being counted as a separate test.
        assertEquals(4, execB.get("total").getAsInt());
        assertEquals(4, execB.get("passed").getAsInt());
        // The earlier failure is fully superseded by the later pass.
        assertEquals(0, execB.get("failed").getAsInt());

        JsonArray trendsAfterStageB = readJsonArray(dataDir.resolve("trends.json"));
        assertEquals(1, trendsAfterStageB.size());

        JsonObject summaryAfterStageB = readJsonObject(dataDir.resolve("summary.json"));
        assertEquals(4, summaryAfterStageB.getAsJsonObject("current").get("total").getAsInt());

        // --- Stage C: a genuinely new regression cycle. In real usage this
        //     means running `mvn clean test` so allure-results only holds
        //     this new cycle's results -- simulated here by clearing the
        //     directory before writing the new suite's results. ---
        try (var existingFiles = Files.list(allureDir)) {
            for (Path path : existingFiles.toList()) {
                Files.deleteIfExists(path);
            }
        }
        writeResult(allureDir, "5-result.json", "hist-5", "Verify Item Removed", "passed", 1_000, 1_500, null);
        writeEnvironment(allureDir, "Denali_Regression_v2");

        ReportEngine.generate(request);

        JsonArray executionsAfterStageC = readJsonArray(dataDir.resolve("executions.json"));
        // Now two entries: the earlier v1 report is preserved untouched,
        // and v2 is a new entry because the suite value changed.
        assertEquals(2, executionsAfterStageC.size());
        boolean hasV1 = false;
        boolean hasV2 = false;
        for (JsonElement el : executionsAfterStageC) {
            JsonObject exec = el.getAsJsonObject();
            if ("Denali_Regression_v1".equals(exec.get("executionId").getAsString())) {
                hasV1 = true;
                assertEquals(4, exec.get("total").getAsInt());
            }
            if ("Denali_Regression_v2".equals(exec.get("executionId").getAsString())) {
                hasV2 = true;
                assertEquals(1, exec.get("total").getAsInt());
                assertEquals(1, exec.get("passed").getAsInt());
            }
        }
        assertEquals(true, hasV1);
        assertEquals(true, hasV2);

        // Now that a second, distinct execution (v2) exists, the comparison
        // becomes available and reflects v2 (1 total) vs. v1 (4 total).
        JsonObject summaryAfterStageC = readJsonObject(dataDir.resolve("summary.json"));
        JsonObject comparisonAfterStageC = summaryAfterStageC.getAsJsonObject("comparison");
        JsonObject totalComparison = comparisonAfterStageC.getAsJsonObject("total");
        assertTrue(totalComparison.get("available").getAsBoolean());
        assertEquals(-3.0, totalComparison.get("value").getAsDouble());
        assertEquals("down", totalComparison.get("direction").getAsString());
    }

    private static void writeResult(Path dir, String fileName, String historyId, String name,
                                     String status, long start, long stop, String failureMessage) throws IOException {
        StringBuilder json = new StringBuilder();
        json.append("{\n")
                .append("  \"uuid\": \"").append(java.util.UUID.randomUUID()).append("\",\n")
                .append("  \"historyId\": \"").append(historyId).append("\",\n")
                .append("  \"name\": \"").append(name).append("\",\n")
                .append("  \"fullName\": \"com.example.Sample#").append(historyId).append("\",\n")
                .append("  \"status\": \"").append(status).append("\",\n")
                .append("  \"start\": ").append(start).append(",\n")
                .append("  \"stop\": ").append(stop).append(",\n")
                .append("  \"labels\": [ { \"name\": \"feature\", \"value\": \"Sample\" } ]");
        if (failureMessage != null) {
            json.append(",\n  \"statusDetails\": { \"message\": \"").append(failureMessage)
                    .append("\", \"trace\": \"java.lang.AssertionError: ").append(failureMessage).append("\" }");
        }
        json.append("\n}\n");
        Files.writeString(dir.resolve(fileName), json.toString(), StandardCharsets.UTF_8);
    }

    private static void writeEnvironment(Path dir, String suite) throws IOException {
        Files.writeString(dir.resolve("environment.properties"),
                "os=Linux\nsuite=" + suite + "\n", StandardCharsets.UTF_8);
    }

    private static JsonArray readJsonArray(Path path) throws IOException {
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonArray();
        }
    }

    private static JsonObject readJsonObject(Path path) throws IOException {
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}
