package com.resideo.tests.hooks;

import com.resideo.dashboard.cucumber.DashboardCucumberPlugin;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

/**
 * Hooks to demonstrate dashboard integration.
 * When the DashboardCucumberPlugin is active via cucumber.properties,
 * results are streamed live. This class adds additional logging.
 */
public class DashboardHooks {

    @Before
    public void beforeScenario(Scenario scenario) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("▶ Scenario: " + scenario.getName());
        System.out.println("  Tags: " + scenario.getSourceTagNames());
        System.out.println("═══════════════════════════════════════════");
    }

    @After
    public void afterScenario(Scenario scenario) {
        String status = scenario.isFailed() ? "❌ FAILED" : "✅ PASSED";
        System.out.println("───────────────────────────────────────────");
        System.out.println("  Result: " + status);
        System.out.println("───────────────────────────────────────────");
    }
}
