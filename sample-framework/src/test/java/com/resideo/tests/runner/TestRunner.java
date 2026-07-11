package com.resideo.tests.runner;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

/**
 * Cucumber Test Runner.
 *
 * Runs all feature files from src/test/resources/features.
 * Results are output to target/cucumber.json for the dashboard to ingest.
 *
 * To run with live dashboard streaming:
 *   mvn test -Dresideo.execution.id=$(uuidgen) -Dresideo.dashboard.url=http://localhost:8080
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty,json:target/cucumber.json")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.resideo.tests")
public class TestRunner {
}
