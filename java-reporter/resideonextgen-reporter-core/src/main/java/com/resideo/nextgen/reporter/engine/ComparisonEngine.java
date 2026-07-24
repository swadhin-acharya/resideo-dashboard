package com.resideo.nextgen.reporter.engine;

import com.google.gson.JsonObject;
import com.resideo.nextgen.reporter.json.JsonViewBuilder;
import com.resideo.nextgen.reporter.model.ExecutionSummary;

import java.util.List;
import java.util.Objects;

/**
 * Computes the "vs previous run" deltas shown on the KPI cards, from the
 * execution history already merged by {@link HistoryStore}. Per the project
 * brief ("where comparison data is unavailable, don't invent it"), every
 * metric is marked unavailable -- never defaulted to zero/flat and
 * presented as if it were real -- when there's no earlier execution to
 * compare against.
 */
public final class ComparisonEngine {

    private ComparisonEngine() {
    }

    public static JsonObject compute(List<ExecutionSummary> history, ExecutionSummary current) {
        ExecutionSummary previous = history.stream()
                .filter(e -> !Objects.equals(e.getExecutionId(), current.getExecutionId()))
                .findFirst()
                .orElse(null);

        boolean available = previous != null;

        JsonObject comparison = new JsonObject();
        comparison.add("total", metric(current.getTotal(), available ? previous.getTotal() : 0, available));
        comparison.add("passed", metric(current.getPassed(), available ? previous.getPassed() : 0, available));
        comparison.add("failed", metric(current.getFailed(), available ? previous.getFailed() : 0, available));
        comparison.add("passRate", metric(current.getPassRate(), available ? previous.getPassRate() : 0, available));
        comparison.add("duration", metric(current.getDurationMs(), available ? previous.getDurationMs() : 0, available));
        return comparison;
    }

    private static JsonObject metric(double currentValue, double previousValue, boolean available) {
        if (!available) {
            return JsonViewBuilder.comparisonMetric(0, false);
        }
        return JsonViewBuilder.comparisonMetric(currentValue - previousValue, true);
    }
}
