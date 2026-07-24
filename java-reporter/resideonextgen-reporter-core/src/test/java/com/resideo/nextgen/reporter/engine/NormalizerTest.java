package com.resideo.nextgen.reporter.engine;

import com.resideo.nextgen.reporter.model.ResultStatus;
import com.resideo.nextgen.reporter.model.TestResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure, no-file-I/O tests for the single calculation layer. These exercise
 * exactly the totals/pass-rate math and the retry-collapsing behavior that
 * everything else (history merge, JSON output, the dashboard UI) depends on
 * being correct.
 */
class NormalizerTest {

    @Test
    void passRateOfHandlesZeroTotalAndRounds() {
        assertEquals(0.0, Normalizer.passRateOf(0, 0));
        assertEquals(100.0, Normalizer.passRateOf(5, 5));
        // 11/15 = 73.333...% -> rounded to 2 decimals, matching the acceptance-test
        // style example from the project brief (57 tests, 53 passed -> 92.98%).
        assertEquals(73.33, Normalizer.passRateOf(11, 15));
    }

    @Test
    void overallStatusPrefersFailedThenBrokenThenPassed() {
        assertEquals(ResultStatus.UNKNOWN, Normalizer.overallStatus(0, 0, 0));
        assertEquals(ResultStatus.PASSED, Normalizer.overallStatus(0, 0, 5));
        assertEquals(ResultStatus.BROKEN, Normalizer.overallStatus(0, 1, 5));
        assertEquals(ResultStatus.FAILED, Normalizer.overallStatus(1, 1, 5));
    }

    @Test
    void buildExecutionSummaryProducesExactTotals() {
        List<TestResult> results = new ArrayList<>();
        results.add(result("t1", ResultStatus.PASSED));
        results.add(result("t2", ResultStatus.PASSED));
        results.add(result("t3", ResultStatus.PASSED));
        results.add(result("t4", ResultStatus.FAILED));
        results.add(result("t5", ResultStatus.BROKEN));

        var summary = Normalizer.buildExecutionSummary(
                "exec-1", "Test Execution", results, 1_000L, 6_000L, null, null);

        assertEquals(5, summary.getTotal());
        assertEquals(3, summary.getPassed());
        assertEquals(1, summary.getFailed());
        assertEquals(1, summary.getBroken());
        assertEquals(0, summary.getSkipped());
        assertEquals(60.0, summary.getPassRate());
        assertEquals(ResultStatus.FAILED, summary.getStatus());
        assertEquals(5_000L, summary.getDurationMs());
    }

    @Test
    void collapseRetriesKeepsOnlyLatestAttemptPerHistoryId() {
        TestResult firstAttempt = result("checkout", ResultStatus.FAILED);
        firstAttempt.setHistoryId("hist-checkout");
        firstAttempt.setStartTime(1_000L);
        firstAttempt.setStopTime(2_000L);

        TestResult rerun = result("checkout", ResultStatus.PASSED);
        rerun.setHistoryId("hist-checkout");
        rerun.setStartTime(5_000L);
        rerun.setStopTime(5_500L);

        TestResult unrelated = result("login", ResultStatus.PASSED);
        unrelated.setHistoryId("hist-login");

        List<TestResult> collapsed = Normalizer.collapseRetries(List.of(firstAttempt, rerun, unrelated));

        // Two distinct tests remain, not three -- the rerun superseded the
        // original failed attempt rather than being counted separately.
        assertEquals(2, collapsed.size());

        TestResult canonicalCheckout = collapsed.stream()
                .filter(r -> "hist-checkout".equals(r.getHistoryId()))
                .findFirst()
                .orElseThrow();
        assertEquals(ResultStatus.PASSED, canonicalCheckout.getStatus());
        assertEquals(1, canonicalCheckout.getRetryCount());

        TestResult canonicalLogin = collapsed.stream()
                .filter(r -> "hist-login".equals(r.getHistoryId()))
                .findFirst()
                .orElseThrow();
        assertEquals(0, canonicalLogin.getRetryCount());
    }

    @Test
    void collapseRetriesNeverDropsResultsWithNoStableIdentity() {
        TestResult a = new TestResult();
        a.setId("uuid-a");
        a.setStatus(ResultStatus.PASSED);
        TestResult b = new TestResult();
        b.setId("uuid-b");
        b.setStatus(ResultStatus.PASSED);

        List<TestResult> collapsed = Normalizer.collapseRetries(List.of(a, b));
        assertEquals(2, collapsed.size());
        assertTrue(collapsed.stream().allMatch(r -> r.getRetryCount() == 0));
    }

    @Test
    void featureAndCategorySummariesGroupCorrectly() {
        TestResult loginPass = result("login-1", ResultStatus.PASSED);
        loginPass.setFeature("Login");
        TestResult loginFail = result("login-2", ResultStatus.FAILED);
        loginFail.setFeature("Login");
        loginFail.setErrorMessage("expected 1 but was 2");
        loginFail.setStackTrace("java.lang.AssertionError: expected 1 but was 2");
        TestResult cartPass = result("cart-1", ResultStatus.PASSED);
        cartPass.setFeature("Cart");

        List<TestResult> results = List.of(loginPass, loginFail, cartPass);

        var features = Normalizer.buildFeatureSummaries(results);
        assertEquals(2, features.size());
        var loginFeature = features.stream().filter(f -> "Login".equals(f.getName())).findFirst().orElseThrow();
        assertEquals(2, loginFeature.getTotal());
        assertEquals(1, loginFeature.getFailed());
        assertEquals(50.0, loginFeature.getPassRate());

        // No categories.json rules supplied -> falls back to exception-class grouping.
        var categories = Normalizer.buildCategorySummaries(results, List.of());
        assertEquals(1, categories.size());
        assertEquals("AssertionError", categories.get(0).getName());
        assertEquals(1, categories.get(0).getCount());
    }

    private static TestResult result(String name, ResultStatus status) {
        TestResult r = new TestResult();
        r.setId(name);
        r.setHistoryId(name);
        r.setName(name);
        r.setFullName(name);
        r.setStatus(status);
        return r;
    }
}
