package com.resideo.nextgen.reporter.allure;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.resideo.nextgen.reporter.model.CategoryRule;
import com.resideo.nextgen.reporter.model.EnvironmentInfo;
import com.resideo.nextgen.reporter.model.ExecutorInfo;
import com.resideo.nextgen.reporter.model.ResultStatus;
import com.resideo.nextgen.reporter.model.StepResult;
import com.resideo.nextgen.reporter.model.TestResult;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * Reads a standard Allure2 {@code allure-results} directory -- the same
 * structured output any Allure adapter (allure-testng, allure-cucumber7-jvm,
 * allure-junit5, ...) produces -- directly into the canonical model. This is
 * the *only* ingestion path for ResideoNextGen's Java integration: Allure is
 * always the source of truth, so this class never re-derives Allure's own
 * report and never invents a result of its own.
 */
public final class AllureResultReader {

    private AllureResultReader() {
    }

    public static boolean hasResults(File allureResultsDir) {
        if (allureResultsDir == null || !allureResultsDir.isDirectory()) {
            return false;
        }
        File[] files = allureResultsDir.listFiles((dir, name) -> name.endsWith("-result.json"));
        return files != null && files.length > 0;
    }

    public static List<TestResult> readResults(File allureResultsDir) throws IOException {
        List<TestResult> results = new ArrayList<>();
        File[] files = allureResultsDir.listFiles((dir, name) -> name.endsWith("-result.json"));
        if (files == null) {
            return results;
        }
        for (File file : files) {
            JsonObject root = parseJsonObject(file);
            if (root != null) {
                results.add(toTestResult(root));
            }
        }
        return results;
    }

    public static EnvironmentInfo readEnvironment(File allureResultsDir) {
        EnvironmentInfo env = new EnvironmentInfo();
        Map<String, String> props = readPropertiesCaseInsensitive(allureResultsDir, "environment.properties");
        env.setOs(props.get("os"));
        env.setJavaVersion(props.getOrDefault("java.version", props.get("java")));
        env.setPlatform(props.get("platform"));
        env.setBrowser(props.get("browser"));
        env.setFramework(props.get("framework"));
        env.setBranch(props.get("branch"));
        env.setBuild(props.get("build"));
        env.setMachine(props.get("machine"));
        env.setDevice(props.get("device"));
        return env;
    }

    /**
     * Loads a .properties file with lowercase-normalized keys, so writers
     * that use Title-Case or UPPERCASE property names (e.g. "OS", "Browser")
     * -- a common style for human-edited environment.properties files --
     * are read the same as lowercase ones. Returns an empty map if the file
     * is missing or unreadable.
     */
    private static Map<String, String> readPropertiesCaseInsensitive(File dir, String fileName) {
        Map<String, String> result = new LinkedHashMap<>();
        File propsFile = new File(dir, fileName);
        if (!propsFile.isFile()) {
            return result;
        }
        Properties props = new Properties();
        try (Reader reader = Files.newBufferedReader(propsFile.toPath(), StandardCharsets.UTF_8)) {
            props.load(reader);
        } catch (IOException ignored) {
            return result;
        }
        for (String key : props.stringPropertyNames()) {
            result.put(key.toLowerCase(Locale.ROOT), props.getProperty(key));
        }
        return result;
    }

    /**
     * Reads an explicit report-grouping key from environment.properties, if
     * present ({@code resideonextgen.suite}, falling back to plain
     * {@code suite}) -- e.g. {@code suite=Denali_Regression_v1}. This is
     * deliberately distinct from a per-test Allure "suite" label: it is an
     * execution-level identifier, not a test-level one.
     *
     * <p>When set, every {@code mvn test} invocation that writes into the
     * same allure-results directory with the same value keeps merging into
     * one ResideoNextGen execution/report entry -- run a subset of tests,
     * run another subset, rerun a few failures, and as long as this value
     * and the allure-results directory (not wiped by {@code mvn clean} in
     * between) stay the same, it all accumulates under one report. Changing
     * the value starts a new report entry.
     */
    public static String readReportGroupKey(File allureResultsDir) {
        Map<String, String> props = readPropertiesCaseInsensitive(allureResultsDir, "environment.properties");
        String value = props.get("resideonextgen.suite");
        if (value == null || value.isBlank()) {
            value = props.get("suite");
        }
        return (value != null && !value.isBlank()) ? value.strip() : null;
    }

    public static ExecutorInfo readExecutor(File allureResultsDir) {
        File executorFile = new File(allureResultsDir, "executor.json");
        if (!executorFile.isFile()) {
            return null;
        }
        JsonObject root = parseJsonObject(executorFile);
        if (root == null) {
            return null;
        }
        ExecutorInfo executor = new ExecutorInfo();
        executor.setName(stringOrNull(root, "name"));
        executor.setType(stringOrNull(root, "type"));
        executor.setBuildName(stringOrNull(root, "buildName"));
        executor.setBuildOrder(root.has("buildOrder") && !root.get("buildOrder").isJsonNull()
                ? String.valueOf(root.get("buildOrder").getAsLong()) : null);
        executor.setBuildUrl(stringOrNull(root, "buildUrl"));
        return executor;
    }

    /** Custom failure-categorization rules from categories.json, if present. */
    public static List<CategoryRule> readCategoryRules(File allureResultsDir) {
        List<CategoryRule> rules = new ArrayList<>();
        File file = new File(allureResultsDir, "categories.json");
        if (!file.isFile()) {
            return rules;
        }
        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            JsonElement el = JsonParser.parseReader(reader);
            if (!el.isJsonArray()) {
                return rules;
            }
            for (JsonElement item : el.getAsJsonArray()) {
                if (!item.isJsonObject()) {
                    continue;
                }
                JsonObject obj = item.getAsJsonObject();
                CategoryRule rule = new CategoryRule();
                rule.setName(stringOrNull(obj, "name"));
                rule.setMessageRegex(stringOrNull(obj, "messageRegex"));
                rule.setTraceRegex(stringOrNull(obj, "traceRegex"));
                List<String> statuses = new ArrayList<>();
                if (obj.has("matchedStatuses") && obj.get("matchedStatuses").isJsonArray()) {
                    for (JsonElement s : obj.getAsJsonArray("matchedStatuses")) {
                        statuses.add(s.getAsString());
                    }
                }
                rule.setMatchedStatuses(statuses);
                rules.add(rule);
            }
        } catch (IOException ignored) {
            // No usable categories.json -> Normalizer falls back to exception-class grouping.
        }
        return rules;
    }

    private static TestResult toTestResult(JsonObject root) {
        TestResult result = new TestResult();
        result.setId(stringOrNull(root, "uuid"));
        result.setHistoryId(stringOrNull(root, "historyId"));
        result.setName(stringOrNull(root, "name"));
        result.setFullName(stringOrNull(root, "fullName"));
        result.setStatus(parseStatus(stringOrNull(root, "status")));
        result.setStartTime(longOrZero(root, "start"));
        result.setStopTime(longOrZero(root, "stop"));
        result.setDurationMs(Math.max(0, result.getStopTime() - result.getStartTime()));

        if (root.has("statusDetails") && root.get("statusDetails").isJsonObject()) {
            JsonObject details = root.getAsJsonObject("statusDetails");
            result.setErrorMessage(stringOrNull(details, "message"));
            result.setStackTrace(stringOrNull(details, "trace"));
        }

        if (root.has("labels") && root.get("labels").isJsonArray()) {
            for (JsonElement el : root.getAsJsonArray("labels")) {
                if (!el.isJsonObject()) {
                    continue;
                }
                JsonObject label = el.getAsJsonObject();
                String labelName = stringOrNull(label, "name");
                String labelValue = stringOrNull(label, "value");
                if (labelName == null) {
                    continue;
                }
                result.getLabels().add(labelName + ":" + labelValue);
                switch (labelName) {
                    case "feature":
                    case "epic":
                        if (result.getFeature() == null) {
                            result.setFeature(labelValue);
                        }
                        break;
                    case "suite":
                        result.setSuite(labelValue);
                        break;
                    case "severity":
                        result.setSeverity(labelValue);
                        break;
                    default:
                        break;
                }
            }
        }

        if (root.has("parameters") && root.get("parameters").isJsonArray()) {
            for (JsonElement el : root.getAsJsonArray("parameters")) {
                if (!el.isJsonObject()) {
                    continue;
                }
                JsonObject param = el.getAsJsonObject();
                String pname = stringOrNull(param, "name");
                String pvalue = stringOrNull(param, "value");
                if (pname != null) {
                    result.getParameters().put(pname, pvalue);
                }
            }
        }

        if (root.has("steps") && root.get("steps").isJsonArray()) {
            for (JsonElement el : root.getAsJsonArray("steps")) {
                if (el.isJsonObject()) {
                    result.getSteps().add(toStepResult(el.getAsJsonObject()));
                }
            }
        }

        return result;
    }

    private static StepResult toStepResult(JsonObject node) {
        String name = stringOrNull(node, "name");
        ResultStatus status = parseStatus(stringOrNull(node, "status"));
        long start = longOrZero(node, "start");
        long stop = longOrZero(node, "stop");
        StepResult step = new StepResult(name, status, Math.max(0, stop - start));
        if (node.has("steps") && node.get("steps").isJsonArray()) {
            for (JsonElement el : node.getAsJsonArray("steps")) {
                if (el.isJsonObject()) {
                    step.getSteps().add(toStepResult(el.getAsJsonObject()));
                }
            }
        }
        return step;
    }

    public static ResultStatus parseStatus(String raw) {
        if (raw == null) {
            return ResultStatus.UNKNOWN;
        }
        switch (raw.toLowerCase(Locale.ROOT)) {
            case "passed":
                return ResultStatus.PASSED;
            case "failed":
                return ResultStatus.FAILED;
            case "broken":
                return ResultStatus.BROKEN;
            case "skipped":
                return ResultStatus.SKIPPED;
            default:
                return ResultStatus.UNKNOWN;
        }
    }

    private static JsonObject parseJsonObject(File file) {
        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            JsonElement el = JsonParser.parseReader(reader);
            return el.isJsonObject() ? el.getAsJsonObject() : null;
        } catch (IOException e) {
            return null;
        }
    }

    private static String stringOrNull(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return null;
        }
        JsonElement el = obj.get(key);
        return el.isJsonPrimitive() ? el.getAsString() : el.toString();
    }

    private static long longOrZero(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return 0L;
        }
        try {
            return obj.get(key).getAsLong();
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
