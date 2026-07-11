package com.sample.hooks;

import com.sample.driver.DriverFactory;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

public class Hooks {

    @Before
    public void beforeScenario(Scenario scenario) throws InterruptedException {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("▶ Scenario: " + scenario.getName());
        System.out.println("  Tags: " + scenario.getSourceTagNames());
        System.out.println("═══════════════════════════════════════════");
        Thread.sleep(2000);
    }

    @After
    public void afterScenario(Scenario scenario) {
        String status = scenario.isFailed() ? "FAILED" : "PASSED";
        System.out.println("───────────────────────────────────────────");
        System.out.println("  Result: " + status);
        System.out.println("───────────────────────────────────────────");
    }

    @Before(order = 0)
    public void beforeAll() {
        DriverFactory.createDriver();
    }

    @After(order = 0)
    public void afterAll() {
        DriverFactory.quitDriver();
    }
}
