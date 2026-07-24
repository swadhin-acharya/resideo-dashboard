package com.resideo.nextgen.reporter.allure;

import com.resideo.nextgen.reporter.model.EnvironmentInfo;
import com.resideo.nextgen.reporter.model.ResultStatus;
import com.resideo.nextgen.reporter.model.TestResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AllureResultReaderTest {

    @Test
    void hasResultsIsFalseForMissingOrEmptyDirectory(@TempDir Path dir) {
        assertFalse(AllureResultReader.hasResults(dir.resolve("does-not-exist").toFile()));
        assertFalse(AllureResultReader.hasResults(dir.toFile()));
    }

    @Test
    void readsResultFileIntoCanonicalModel(@TempDir Path dir) throws IOException {
        writeFile(dir, "abc-result.json", """
                {
                  "uuid": "11111111-1111-1111-1111-111111111111",
                  "historyId": "hist-1",
                  "name": "Verify Login",
                  "fullName": "com.example.LoginTest#verifyLogin",
                  "status": "failed",
                  "start": 1000,
                  "stop": 2500,
                  "statusDetails": {
                    "message": "expected 42.00 but was 41.50",
                    "trace": "java.lang.AssertionError: expected 42.00 but was 41.50"
                  },
                  "labels": [
                    { "name": "feature", "value": "Login" },
                    { "name": "severity", "value": "critical" }
                  ],
                  "parameters": [
                    { "name": "username", "value": "standard_user" }
                  ],
                  "steps": [
                    { "name": "Open login page", "status": "passed", "start": 1000, "stop": 1200 },
                    { "name": "Submit credentials", "status": "failed", "start": 1200, "stop": 2500 }
                  ]
                }
                """);

        assertTrue(AllureResultReader.hasResults(dir.toFile()));
        List<TestResult> results = AllureResultReader.readResults(dir.toFile());
        assertEquals(1, results.size());

        TestResult r = results.get(0);
        assertEquals("hist-1", r.getHistoryId());
        assertEquals("Verify Login", r.getName());
        assertEquals(ResultStatus.FAILED, r.getStatus());
        assertEquals(1500L, r.getDurationMs());
        assertEquals("Login", r.getFeature());
        assertEquals("critical", r.getSeverity());
        assertEquals("standard_user", r.getParameters().get("username"));
        assertEquals(2, r.getSteps().size());
        assertEquals(ResultStatus.FAILED, r.getSteps().get(1).getStatus());
        assertTrue(r.getErrorMessage().contains("expected 42.00"));
    }

    @Test
    void readEnvironmentIsCaseInsensitive(@TempDir Path dir) throws IOException {
        // Deliberately Title-Case / UPPERCASE keys -- a common human-edited
        // environment.properties style that must still be read correctly.
        writeFile(dir, "environment.properties", """
                OS=Linux 6.5
                Browser=Chrome (headless)
                Platform=Web
                """);

        EnvironmentInfo env = AllureResultReader.readEnvironment(dir.toFile());
        assertEquals("Linux 6.5", env.getOs());
        assertEquals("Chrome (headless)", env.getBrowser());
        assertEquals("Web", env.getPlatform());
    }

    @Test
    void readReportGroupKeyPrefersNamespacedProperty(@TempDir Path dir) throws IOException {
        writeFile(dir, "environment.properties", """
                suite=Denali_Regression_v1
                resideonextgen.suite=Denali_Regression_v2
                """);
        assertEquals("Denali_Regression_v2", AllureResultReader.readReportGroupKey(dir.toFile()));
    }

    @Test
    void readReportGroupKeyFallsBackToPlainSuiteKey(@TempDir Path dir) throws IOException {
        writeFile(dir, "environment.properties", "suite=Denali_Regression_v1\n");
        assertEquals("Denali_Regression_v1", AllureResultReader.readReportGroupKey(dir.toFile()));
    }

    @Test
    void readReportGroupKeyIsNullWhenNotSet(@TempDir Path dir) throws IOException {
        writeFile(dir, "environment.properties", "os=Linux\n");
        assertNull(AllureResultReader.readReportGroupKey(dir.toFile()));
    }

    private static void writeFile(Path dir, String name, String content) throws IOException {
        Files.writeString(dir.resolve(name), content, StandardCharsets.UTF_8);
    }
}
