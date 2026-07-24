package com.resideo.nextgen.reporter.engine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.resideo.nextgen.reporter.model.ExecutionSummary;
import com.resideo.nextgen.reporter.model.FailureSummary;
import com.resideo.nextgen.reporter.model.TestResult;
import com.resideo.nextgen.reporter.model.TrendPoint;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Merges a single execution's results into this project's persisted
 * ResideoNextGen history. Mirrors the Node processor's history.ts merge
 * semantics: reprocessing the same executionId replaces that entry rather
 * than duplicating it or double-counting its failures, so re-running
 * against the same allure-results directory is always safe.
 *
 * <p>This class owns the *internal* history state only (dot-prefixed files,
 * holding the full-detail internal model), returning the merged lists so
 * the caller (see {@link ReportEngine}) can compute comparisons and map
 * them through {@link com.resideo.nextgen.reporter.json.JsonViewBuilder}
 * into the public {@code executions.json} / {@code trends.json} /
 * {@code failures.json} the frontend actually fetches. Those two shapes are
 * deliberately different: the frontend's canonical model
 * (src/types/models.ts) is leaner and uses different field names/types
 * (ISO date strings, lowercase status, "duration" not "durationMs", etc.)
 * than what's convenient to keep merging on internally.
 */
public final class HistoryStore {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private HistoryStore() {
    }

    public static List<ExecutionSummary> mergeExecutions(
            Path dataDir, ExecutionSummary latest, int historyLimit) throws IOException {

        Path historyPath = dataDir.resolve(".history-executions.json");
        List<ExecutionSummary> history = readList(historyPath, ExecutionSummary.class);
        history.removeIf(e -> Objects.equals(e.getExecutionId(), latest.getExecutionId()));
        history.add(latest);
        history.sort((a, b) -> Long.compare(b.getStartTime(), a.getStartTime()));

        if (historyLimit > 0 && history.size() > historyLimit) {
            history = new ArrayList<>(history.subList(0, historyLimit));
        }

        writeList(historyPath, history);
        return history;
    }

    public static List<TrendPoint> mergeTrends(Path dataDir, TrendPoint latest, int historyLimit) throws IOException {
        Path historyPath = dataDir.resolve(".history-trends.json");
        List<TrendPoint> trends = readList(historyPath, TrendPoint.class);
        trends.removeIf(t -> Objects.equals(t.getExecutionId(), latest.getExecutionId()));
        trends.add(latest);
        trends.sort(Comparator.comparingLong(TrendPoint::getTimestamp));

        if (historyLimit > 0 && trends.size() > historyLimit) {
            trends = new ArrayList<>(trends.subList(trends.size() - historyLimit, trends.size()));
        }

        writeList(historyPath, trends);
        return trends;
    }

    /**
     * Idempotently folds this execution's failing tests into the persisted
     * top-failures list. A {@code .failures-contributions.json} bookkeeping
     * file (not consumed by the dashboard) tracks which executionIds have
     * already been counted for each testId, so reprocessing the same
     * execution never double-counts an occurrence.
     *
     * @param timestamp the current execution's start time (epoch millis),
     *                   recorded as each newly-contributing failure's
     *                   "last seen" time.
     */
    public static List<FailureSummary> mergeFailures(
            Path dataDir, String executionId, List<TestResult> failingResults, long timestamp) throws IOException {

        Path contributionsPath = dataDir.resolve(".failures-contributions.json");
        Map<String, Set<String>> contributions = readContributions(contributionsPath);

        Path historyPath = dataDir.resolve(".history-failures.json");
        List<FailureSummary> failures = readList(historyPath, FailureSummary.class);
        Map<String, FailureSummary> byTestId = new LinkedHashMap<>();
        for (FailureSummary f : failures) {
            byTestId.put(f.getTestId(), f);
        }

        for (TestResult r : failingResults) {
            String testId = r.getHistoryId() != null ? r.getHistoryId() : r.getFullName();
            if (testId == null) {
                continue;
            }

            Set<String> seenExecutions = contributions.computeIfAbsent(testId, k -> new LinkedHashSet<>());
            if (seenExecutions.contains(executionId)) {
                continue; // already counted for this execution -- reprocessing is a no-op here
            }
            seenExecutions.add(executionId);

            FailureSummary summary = byTestId.computeIfAbsent(testId, k -> {
                FailureSummary fs = new FailureSummary();
                fs.setTestId(testId);
                fs.setTestName(r.getName());
                fs.setFeature(r.getFeature());
                fs.setOccurrences(0);
                return fs;
            });
            summary.setOccurrences(summary.getOccurrences() + 1);
            summary.setLastSeenExecutionId(executionId);
            summary.setLastSeenAt(timestamp);
        }

        List<FailureSummary> sorted = byTestId.values().stream()
                .sorted((a, b) -> Integer.compare(b.getOccurrences(), a.getOccurrences()))
                .collect(Collectors.toList());

        writeList(historyPath, sorted);
        writeContributions(contributionsPath, contributions);
        return sorted;
    }

    private static Map<String, Set<String>> readContributions(Path path) throws IOException {
        if (!Files.isRegularFile(path)) {
            return new LinkedHashMap<>();
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Type type = new TypeToken<LinkedHashMap<String, LinkedHashSet<String>>>() {
            }.getType();
            Map<String, Set<String>> parsed = GSON.fromJson(reader, type);
            return parsed != null ? parsed : new LinkedHashMap<>();
        }
    }

    private static void writeContributions(Path path, Map<String, Set<String>> contributions) throws IOException {
        Files.createDirectories(path.getParent());
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            GSON.toJson(contributions, writer);
        }
    }

    private static <T> List<T> readList(Path path, Class<T> type) throws IOException {
        if (!Files.isRegularFile(path)) {
            return new ArrayList<>();
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonElement el = JsonParser.parseReader(reader);
            if (!el.isJsonArray()) {
                return new ArrayList<>();
            }
            List<T> result = new ArrayList<>();
            for (JsonElement item : el.getAsJsonArray()) {
                result.add(GSON.fromJson(item, type));
            }
            return result;
        }
    }

    private static <T> void writeList(Path path, List<T> items) throws IOException {
        Files.createDirectories(path.getParent());
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            GSON.toJson(items, writer);
        }
    }
}
