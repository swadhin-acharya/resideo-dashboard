package com.resideo.nextgen.reporter.json;

import com.google.gson.JsonObject;
import com.resideo.nextgen.reporter.model.ResultStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure-function coverage for the mapping layer that translates this
 * module's internal model into exactly the JSON shapes
 * src/types/models.ts declares. See the class Javadoc on
 * {@link JsonViewBuilder} for why this mapping exists as its own layer.
 */
class JsonViewBuilderTest {

    @Test
    void statusIsLowercasedToMatchFrontendStringUnion() {
        assertEquals("passed", JsonViewBuilder.status(ResultStatus.PASSED));
        assertEquals("failed", JsonViewBuilder.status(ResultStatus.FAILED));
        assertEquals("broken", JsonViewBuilder.status(ResultStatus.BROKEN));
        assertEquals("skipped", JsonViewBuilder.status(ResultStatus.SKIPPED));
        assertEquals("unknown", JsonViewBuilder.status(ResultStatus.UNKNOWN));
        assertEquals("unknown", JsonViewBuilder.status(null));
    }

    @Test
    void isoTimeProducesAParsableIsoInstantString() {
        String iso = JsonViewBuilder.isoTime(0L);
        assertEquals("1970-01-01T00:00:00Z", iso);
    }

    @Test
    void comparisonMetricDirectionMatchesSignOfValue() {
        JsonObject up = JsonViewBuilder.comparisonMetric(5.0, true);
        assertEquals("up", up.get("direction").getAsString());
        assertTrue(up.get("available").getAsBoolean());

        JsonObject down = JsonViewBuilder.comparisonMetric(-2.5, true);
        assertEquals("down", down.get("direction").getAsString());

        JsonObject flat = JsonViewBuilder.comparisonMetric(0.0, true);
        assertEquals("flat", flat.get("direction").getAsString());

        JsonObject unavailable = JsonViewBuilder.comparisonMetric(0.0, false);
        assertFalse(unavailable.get("available").getAsBoolean());
    }

    @Test
    void executorInfoOmitsBuildOrderWhenNonNumeric() {
        com.resideo.nextgen.reporter.model.ExecutorInfo executor = new com.resideo.nextgen.reporter.model.ExecutorInfo();
        executor.setName("Local");
        executor.setBuildOrder("not-a-number");

        JsonObject json = JsonViewBuilder.executorInfo(executor);
        assertEquals("Local", json.get("name").getAsString());
        assertFalse(json.has("buildOrder"), "non-numeric buildOrder must be omitted, not crash or emit garbage");
    }

    @Test
    void environmentInfoOmitsBlankFieldsRatherThanEmittingEmptyStrings() {
        com.resideo.nextgen.reporter.model.EnvironmentInfo env = new com.resideo.nextgen.reporter.model.EnvironmentInfo();
        env.setOs("Linux");
        env.setBrowser("  ");

        JsonObject json = JsonViewBuilder.environmentInfo(env);
        assertEquals("Linux", json.get("os").getAsString());
        assertFalse(json.has("browser"));
        assertFalse(json.has("platform"));
    }
}
