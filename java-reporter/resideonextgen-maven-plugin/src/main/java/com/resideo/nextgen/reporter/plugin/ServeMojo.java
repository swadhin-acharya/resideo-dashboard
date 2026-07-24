package com.resideo.nextgen.reporter.plugin;

import com.resideo.nextgen.reporter.engine.ReportEngine;
import com.resideo.nextgen.reporter.engine.ReportRequest;
import com.resideo.nextgen.reporter.server.BrowserLauncher;
import com.resideo.nextgen.reporter.server.DashboardServer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;

/**
 * Processes this project's most recent Allure results into ResideoNextGen
 * dashboard data and serves it locally.
 *
 * <p>Usage: {@code mvn resideonextgen:serve}
 *
 * <p>Self-sufficient -- requires only that your test framework's Allure
 * adapter has already populated an allure-results directory (from a normal
 * {@code mvn test}); no ResideoNextGen listener/plugin needs to be wired
 * into your test run for this manual path to work. Blocks the Maven
 * process until interrupted (Ctrl+C).
 */
@Mojo(name = "serve", requiresProject = false, threadSafe = true)
public class ServeMojo extends AbstractMojo {

    /** Directory containing Allure's raw results, e.g. target/allure-results. */
    @Parameter(property = "resideonextgen.allureResultsDir")
    private File allureResultsDir;

    /** Directory to write/read generated ResideoNextGen dashboard data. */
    @Parameter(property = "resideonextgen.dataDir")
    private File dataDir;

    /** How many executions of history to retain. */
    @Parameter(property = "resideonextgen.historyLimit", defaultValue = "50")
    private int historyLimit;

    /** Preferred port; 0 (default) picks any free local port. */
    @Parameter(property = "resideonextgen.port", defaultValue = "0")
    private int port;

    /** Skip reprocessing Allure results and just serve whatever dashboard data already exists. */
    @Parameter(property = "resideonextgen.skipProcessing", defaultValue = "false")
    private boolean skipProcessing;

    /** Open the dashboard URL in the default browser once the server starts. */
    @Parameter(property = "resideonextgen.openBrowser", defaultValue = "true")
    private boolean openBrowser;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        File resolvedAllureResultsDir = resolveAllureResultsDir();
        File resolvedDataDir = resolveDataDir();

        if (!skipProcessing) {
            try {
                ReportRequest request = ReportRequest.create()
                        .allureResultsDir(resolvedAllureResultsDir)
                        .dataDir(resolvedDataDir)
                        .historyLimit(historyLimit);
                ReportEngine.generate(request);
                getLog().info("Processed Allure results from " + resolvedAllureResultsDir.getAbsolutePath());
            } catch (IllegalStateException e) {
                if (!resolvedDataDir.isDirectory()) {
                    throw new MojoExecutionException(e.getMessage());
                }
                getLog().warn(e.getMessage());
                getLog().warn("Serving previously generated dashboard data instead.");
            } catch (IOException e) {
                throw new MojoExecutionException("Failed to process Allure results", e);
            }
        }

        if (!resolvedDataDir.isDirectory()) {
            throw new MojoExecutionException(
                    "No ResideoNextGen dashboard data found at " + resolvedDataDir.getAbsolutePath()
                            + ". Run your tests first so Allure results exist, then re-run mvn resideonextgen:serve.");
        }

        try {
            DashboardServer server = DashboardServer.start(resolvedDataDir, port);
            getLog().info("ResideoNextGen Dashboard is live at " + server.getUrl());
            getLog().info("Press Ctrl+C to stop.");
            BrowserLauncher.openQuietly(server.getUrl(), openBrowser);
            server.blockUntilStopped();
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to start ResideoNextGen Dashboard server", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private File resolveAllureResultsDir() {
        if (allureResultsDir != null) {
            return allureResultsDir;
        }
        return new File(projectBaseDir(), "target/allure-results");
    }

    private File resolveDataDir() {
        if (dataDir != null) {
            return dataDir;
        }
        return new File(projectBaseDir(), "target/resideonextgen/data");
    }

    private File projectBaseDir() {
        return (project != null && project.getBasedir() != null) ? project.getBasedir() : new File(".");
    }
}
