package com.resideo.nextgen.reporter.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.resideo.nextgen.reporter.model.CategorySummary;
import com.resideo.nextgen.reporter.model.EnvironmentInfo;
import com.resideo.nextgen.reporter.model.ExecutionSummary;
import com.resideo.nextgen.reporter.model.ExecutorInfo;
import com.resideo.nextgen.reporter.model.FailureSummary;
import com.resideo.nextgen.reporter.model.FeatureSummary;
import com.resideo.nextgen.reporter.model.ResultStatus;
import com.resideo.nextgen.reporter.model.TrendPoint;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

/**
 * Builds the exact JSON shapes the React frontend's canonical TypeScript
 * model ({@code src/types/models.ts}) expects, translating from this
 * module's internal Java model.
 *
 * <p>This is the only place that translation happens, on purpose: the
 * internal model favors calculation-friendly shapes (epoch millis, Java
 * enums, condensed strings), while the frontend contract needs specific
 * field names and types (ISO-8601 date strings, lowercase status values,
 * "duration" not "durationMs", etc). Building explicit {@link JsonObject}s
 * here -- rather than serializing internal model classes directly with
 * Gson's reflection -- makes every field name auditable against
 * src/types/models.ts line-for-line, and was written after a real bug: an
 * earlier version of this module let Gson serialize internal fields
 * directly, which silently produced a completely different shape than the
 * frontend expected (missing "comparison", "durationMs" instead of
 * "duration", uppercase enum names, etc.) and crashed the UI after its
 * first render.
 */
public final class JsonViewBuilder {

    private JsonViewBuilder() {
    }

    public static String status(ResultStatus status) {
        return status == null ? "unknown" : status.name().toLowerCase(Locale.ROOT);
    }

    public static String isoTime(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).toString();
    }

    /** The frontend's ExecutionSummary shape, without environment/executor (see {@link #attachEnvironmentAndExecutor}). */
    public static JsonObject executionSummary(ExecutionSummary e) {
        JsonObject obj = new JsonObject();
        obj.addProperty("total", e.getTotal());
        obj.addProperty("passed", e.getPassed());
        obj.addProperty("failed", e.getFailed());
        obj.addProperty("broken", e.getBroken());
        obj.addProperty("skipped", e.getSkipped());
        obj.addProperty("unknown", e.getUnknown());
        obj.addProperty("executionId", e.getExecutionId());
        obj.addProperty("executionName", e.getExecutionName());
        obj.addProperty("status", status(e.getStatus()));
        obj.addProperty("startTime", isoTime(e.getStartTime()));
        obj.addProperty("endTime", isoTime(e.getEndTime()));
        obj.addProperty("duration", e.getDurationMs());
        obj.addProperty("passRate", e.getPassRate());
        return obj;
    }

    public static void attachEnvironmentAndExecutor(JsonObject executionSummaryJson, EnvironmentInfo environment, ExecutorInfo executor) {
        JsonObject env = environmentInfo(environment);
        if (env.size() > 0) {
            executionSummaryJson.add("environment", env);
        }
        JsonObject exec = executorInfo(executor);
        if (exec != null) {
            executionSummaryJson.add("executor", exec);
        }
    }

    /** The frontend's leaner RecentExecutionRow shape used by executions.json. */
    public static JsonObject recentExecutionRow(ExecutionSummary e) {
        JsonObject obj = new JsonObject();
        obj.addProperty("total", e.getTotal());
        obj.addProperty("passed", e.getPassed());
        obj.addProperty("failed", e.getFailed());
        obj.addProperty("broken", e.getBroken());
        obj.addProperty("skipped", e.getSkipped());
        obj.addProperty("unknown", e.getUnknown());
        obj.addProperty("executionId", e.getExecutionId());
        obj.addProperty("status", status(e.getStatus()));
        obj.addProperty("passRate", e.getPassRate());
        obj.addProperty("duration", e.getDurationMs());
        obj.addProperty("date", isoTime(e.getStartTime()));
        return obj;
    }

    public static JsonArray recentExecutionRows(List<ExecutionSummary> executions) {
        JsonArray array = new JsonArray();
        for (ExecutionSummary e : executions) {
            array.add(recentExecutionRow(e));
        }
        return array;
    }

    public static JsonObject trendPoint(TrendPoint t) {
        JsonObject obj = new JsonObject();
        obj.addProperty("executionId", t.getExecutionId());
        obj.addProperty("date", isoTime(t.getTimestamp()));
        obj.addProperty("total", t.getPassed() + t.getFailed() + t.getBroken() + t.getSkipped());
        obj.addProperty("passed", t.getPassed());
        obj.addProperty("failed", t.getFailed());
        obj.addProperty("broken", t.getBroken());
        obj.addProperty("skipped", t.getSkipped());
        obj.addProperty("passRate", t.getPassRate());
        obj.addProperty("duration", t.getDurationMs());
        return obj;
    }

    public static JsonArray trendPoints(List<TrendPoint> trends) {
        JsonArray array = new JsonArray();
        for (TrendPoint t : trends) {
            array.add(trendPoint(t));
        }
        return array;
    }

    public static JsonObject featureSummary(FeatureSummary f) {
        JsonObject obj = new JsonObject();
        obj.addProperty("total", f.getTotal());
        obj.addProperty("passed", f.getPassed());
        obj.addProperty("failed", f.getFailed());
        obj.addProperty("broken", f.getBroken());
        obj.addProperty("skipped", f.getSkipped());
        obj.addProperty("unknown", 0);
        // No independent stable feature identifier is captured from Allure
        // labels today -- the feature name doubles as its id.
        obj.addProperty("featureId", f.getName());
        obj.addProperty("name", f.getName());
        obj.addProperty("passRate", f.getPassRate());
        return obj;
    }

    public static JsonArray featureSummaries(List<FeatureSummary> features) {
        JsonArray array = new JsonArray();
        for (FeatureSummary f : features) {
            array.add(featureSummary(f));
        }
        return array;
    }

    public static JsonObject failureSummary(FailureSummary f) {
        JsonObject obj = new JsonObject();
        obj.addProperty("testId", f.getTestId());
        obj.addProperty("name", f.getTestName());
        obj.addProperty("feature", f.getFeature());
        obj.addProperty("occurrences", f.getOccurrences());
        if (f.getLastSeenAt() > 0) {
            obj.addProperty("lastSeen", isoTime(f.getLastSeenAt()));
        }
        return obj;
    }

    public static JsonArray failureSummaries(List<FailureSummary> failures) {
        JsonArray array = new JsonArray();
        for (FailureSummary f : failures) {
            array.add(failureSummary(f));
        }
        return array;
    }

    public static JsonObject categorySummary(CategorySummary c) {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", c.getName());
        obj.addProperty("count", c.getCount());
        return obj;
    }

    public static JsonArray categorySummaries(List<CategorySummary> categories) {
        JsonArray array = new JsonArray();
        for (CategorySummary c : categories) {
            array.add(categorySummary(c));
        }
        return array;
    }

    public static JsonObject environmentInfo(EnvironmentInfo env) {
        JsonObject obj = new JsonObject();
        if (env == null) {
            return obj;
        }
        putOrOmit(obj, "os", env.getOs());
        putOrOmit(obj, "java", env.getJavaVersion());
        putOrOmit(obj, "platform", env.getPlatform());
        putOrOmit(obj, "browser", env.getBrowser());
        putOrOmit(obj, "framework", env.getFramework());
        putOrOmit(obj, "branch", env.getBranch());
        putOrOmit(obj, "build", env.getBuild());
        putOrOmit(obj, "machine", env.getMachine());
        // "device" (structured DeviceInfo) is intentionally omitted rather
        // than fabricated -- this module doesn't capture it separately from
        // a free-text environment.properties value today.
        return obj;
    }

    public static JsonObject executorInfo(ExecutorInfo executor) {
        if (executor == null) {
            return null;
        }
        JsonObject obj = new JsonObject();
        putOrOmit(obj, "name", executor.getName());
        putOrOmit(obj, "type", executor.getType());
        putOrOmit(obj, "buildName", executor.getBuildName());
        if (executor.getBuildOrder() != null) {
            try {
                obj.addProperty("buildOrder", Long.parseLong(executor.getBuildOrder()));
            } catch (NumberFormatException ignored) {
                // Non-numeric buildOrder -- omit rather than emit a bad number.
            }
        }
        putOrOmit(obj, "url", executor.getBuildUrl());
        return obj.size() > 0 ? obj : null;
    }

    private static void putOrOmit(JsonObject obj, String key, String value) {
        if (value != null && !value.isBlank()) {
            obj.addProperty(key, value);
        }
    }

    public static JsonObject comparisonMetric(double value, boolean available) {
        JsonObject obj = new JsonObject();
        obj.addProperty("value", value);
        obj.addProperty("direction", value > 0 ? "up" : value < 0 ? "down" : "flat");
        obj.addProperty("available", available);
        return obj;
    }

    public static JsonObject summary(String latestExecutionId, ExecutionSummary current, JsonObject comparison,
                                      EnvironmentInfo environment, ExecutorInfo executor) {
        JsonObject exec = executionSummary(current);
        attachEnvironmentAndExecutor(exec, environment, executor);

        JsonObject obj = new JsonObject();
        obj.addProperty("generatedAt", Instant.now().toString());
        obj.addProperty("latestExecutionId", latestExecutionId);
        obj.add("current", exec);
        obj.add("comparison", comparison);
        return obj;
    }
}
