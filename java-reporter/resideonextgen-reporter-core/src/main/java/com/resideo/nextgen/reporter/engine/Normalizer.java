package com.resideo.nextgen.reporter.engine;

import com.resideo.nextgen.reporter.model.CategoryRule;
import com.resideo.nextgen.reporter.model.CategorySummary;
import com.resideo.nextgen.reporter.model.EnvironmentInfo;
import com.resideo.nextgen.reporter.model.ExecutionSummary;
import com.resideo.nextgen.reporter.model.ExecutorInfo;
import com.resideo.nextgen.reporter.model.FeatureSummary;
import com.resideo.nextgen.reporter.model.ResultStatus;
import com.resideo.nextgen.reporter.model.TestResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * The single calculation layer. Every count, rate, and grouping the
 * dashboard displays is computed exactly once, here, from TestResults
 * parsed straight out of Allure. Nothing downstream (the JSON writer, the
 * embedded server, the React UI) recalculates any of this -- matching the
 * same "one calculation layer" rule the Node processor follows.
 */
public final class Normalizer {

    private Normalizer() {
    }

    /**
     * Collapses reruns of the same test (matched by Allure's historyId, or
     * fullName/id as a fallback when historyId is absent) down to a single
     * canonical result: the attempt with the latest stop time wins, and its
     * retryCount is set to how many earlier attempts it superseded.
     *
     * <p>This is what makes selective/incremental test runs work correctly:
     * if a project accumulates results across several {@code mvn test}
     * invocations into the same allure-results directory (e.g. run a
     * subset, run another subset, rerun a few failures), a test that failed
     * on an earlier attempt and passed on a later one must count once, as
     * passed -- not twice, and not as a failure. Always call this before
     * computing any totals, feature health, or category/failure summaries.
     */
    public static List<TestResult> collapseRetries(List<TestResult> results) {
        Map<String, TestResult> canonical = new LinkedHashMap<>();
        Map<String, Integer> attemptCounts = new LinkedHashMap<>();

        for (TestResult r : results) {
            String key = identityKey(r);
            TestResult existing = canonical.get(key);
            if (existing == null || r.getStopTime() >= existing.getStopTime()) {
                canonical.put(key, r);
            }
            attemptCounts.merge(key, 1, Integer::sum);
        }

        List<TestResult> collapsed = new ArrayList<>(canonical.size());
        for (Map.Entry<String, TestResult> entry : canonical.entrySet()) {
            TestResult r = entry.getValue();
            r.setRetryCount(Math.max(0, attemptCounts.getOrDefault(entry.getKey(), 1) - 1));
            collapsed.add(r);
        }
        return collapsed;
    }

    private static String identityKey(TestResult r) {
        if (r.getHistoryId() != null && !r.getHistoryId().isBlank()) {
            return "history:" + r.getHistoryId();
        }
        if (r.getFullName() != null && !r.getFullName().isBlank()) {
            return "fullName:" + r.getFullName();
        }
        // No stable identity at all -- never collapse, treat as its own unique result.
        return "uuid:" + r.getId();
    }

    public static ExecutionSummary buildExecutionSummary(
            String executionId,
            String executionName,
            List<TestResult> results,
            long suiteStartTime,
            long suiteEndTime,
            EnvironmentInfo environment,
            ExecutorInfo executor) {

        ExecutionSummary summary = new ExecutionSummary();
        summary.setExecutionId(executionId);
        summary.setExecutionName(executionName);
        summary.setStartTime(suiteStartTime);
        summary.setEndTime(suiteEndTime);
        summary.setDurationMs(Math.max(0, suiteEndTime - suiteStartTime));

        int total = results.size();
        int passed = countStatus(results, ResultStatus.PASSED);
        int failed = countStatus(results, ResultStatus.FAILED);
        int broken = countStatus(results, ResultStatus.BROKEN);
        int skipped = countStatus(results, ResultStatus.SKIPPED);
        int unknown = countStatus(results, ResultStatus.UNKNOWN);

        summary.setTotal(total);
        summary.setPassed(passed);
        summary.setFailed(failed);
        summary.setBroken(broken);
        summary.setSkipped(skipped);
        summary.setUnknown(unknown);
        summary.setPassRate(passRateOf(passed, total));
        summary.setStatus(overallStatus(failed, broken, total));

        summary.setEnvironment(environment != null ? summarizeEnvironment(environment) : null);
        summary.setExecutor(executor != null ? executor.getName() : null);

        return summary;
    }

    public static double passRateOf(int passed, int total) {
        if (total <= 0) {
            return 0.0;
        }
        return Math.round((passed * 10000.0) / total) / 100.0;
    }

    public static ResultStatus overallStatus(int failed, int broken, int total) {
        if (total == 0) {
            return ResultStatus.UNKNOWN;
        }
        if (failed > 0) {
            return ResultStatus.FAILED;
        }
        if (broken > 0) {
            return ResultStatus.BROKEN;
        }
        return ResultStatus.PASSED;
    }

    private static int countStatus(List<TestResult> results, ResultStatus status) {
        int count = 0;
        for (TestResult r : results) {
            if (r.getStatus() == status) {
                count++;
            }
        }
        return count;
    }

    private static String summarizeEnvironment(EnvironmentInfo env) {
        List<String> parts = new ArrayList<>();
        addIfPresent(parts, env.getPlatform());
        addIfPresent(parts, env.getBrowser());
        addIfPresent(parts, env.getOs());
        return parts.isEmpty() ? null : String.join(" / ", parts);
    }

    private static void addIfPresent(List<String> parts, String value) {
        if (value != null && !value.isBlank()) {
            parts.add(value);
        }
    }

    public static List<FeatureSummary> buildFeatureSummaries(List<TestResult> results) {
        Map<String, List<TestResult>> byFeature = results.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getFeature() != null ? r.getFeature() : "Unassigned",
                        LinkedHashMap::new, Collectors.toList()));

        List<FeatureSummary> summaries = new ArrayList<>();
        for (Map.Entry<String, List<TestResult>> entry : byFeature.entrySet()) {
            List<TestResult> group = entry.getValue();
            FeatureSummary fs = new FeatureSummary();
            fs.setName(entry.getKey());
            fs.setTotal(group.size());
            fs.setPassed(countStatus(group, ResultStatus.PASSED));
            fs.setFailed(countStatus(group, ResultStatus.FAILED));
            fs.setBroken(countStatus(group, ResultStatus.BROKEN));
            fs.setSkipped(countStatus(group, ResultStatus.SKIPPED));
            fs.setPassRate(passRateOf(fs.getPassed(), fs.getTotal()));
            summaries.add(fs);
        }
        return summaries;
    }

    public static List<CategorySummary> buildCategorySummaries(List<TestResult> results, List<CategoryRule> rules) {
        Map<String, Integer> counts = new LinkedHashMap<>();

        List<TestResult> unmatched = new ArrayList<>();
        for (TestResult r : results) {
            if (r.getStatus() != ResultStatus.FAILED && r.getStatus() != ResultStatus.BROKEN) {
                continue;
            }
            String matchedCategory = matchCategory(r, rules);
            if (matchedCategory != null) {
                counts.merge(matchedCategory, 1, Integer::sum);
            } else {
                unmatched.add(r);
            }
        }

        // Fall back to grouping by exception class name for anything no
        // custom rule matched (or when no categories.json was supplied at
        // all) -- never silently drop a failure from Failure Analysis.
        for (TestResult r : unmatched) {
            String fallback = exceptionClassName(r.getStackTrace(), r.getErrorMessage());
            counts.merge(fallback, 1, Integer::sum);
        }

        return counts.entrySet().stream()
                .map(e -> {
                    CategorySummary cs = new CategorySummary();
                    cs.setName(e.getKey());
                    cs.setCount(e.getValue());
                    return cs;
                })
                .collect(Collectors.toList());
    }

    private static String matchCategory(TestResult r, List<CategoryRule> rules) {
        if (rules == null) {
            return null;
        }
        String statusName = r.getStatus() != null ? r.getStatus().name().toLowerCase(Locale.ROOT) : "";
        for (CategoryRule rule : rules) {
            if (rule.getMatchedStatuses() != null && !rule.getMatchedStatuses().isEmpty()
                    && !rule.getMatchedStatuses().contains(statusName)) {
                continue;
            }
            if (matchesRegex(rule.getMessageRegex(), r.getErrorMessage())
                    || matchesRegex(rule.getTraceRegex(), r.getStackTrace())) {
                return rule.getName();
            }
        }
        return null;
    }

    private static boolean matchesRegex(String regex, String value) {
        if (regex == null || regex.isBlank() || value == null) {
            return false;
        }
        try {
            return Pattern.compile(regex, Pattern.DOTALL).matcher(value).find();
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    private static String exceptionClassName(String stackTrace, String errorMessage) {
        String source = stackTrace != null ? stackTrace : errorMessage;
        if (source == null || source.isBlank()) {
            return "Uncategorized";
        }
        String firstLine = source.strip().split("\\r?\\n", 2)[0];
        int colon = firstLine.indexOf(':');
        String candidate = colon > 0 ? firstLine.substring(0, colon) : firstLine;
        candidate = candidate.strip();
        int lastDot = candidate.lastIndexOf('.');
        return lastDot >= 0 && lastDot < candidate.length() - 1 ? candidate.substring(lastDot + 1) : candidate;
    }

    public static List<TestResult> failuresOnly(List<TestResult> results) {
        return results.stream()
                .filter(r -> r.getStatus() == ResultStatus.FAILED || r.getStatus() == ResultStatus.BROKEN)
                .collect(Collectors.toList());
    }
}
