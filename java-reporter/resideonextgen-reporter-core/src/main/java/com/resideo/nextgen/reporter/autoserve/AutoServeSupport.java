package com.resideo.nextgen.reporter.autoserve;

import com.resideo.nextgen.reporter.server.BrowserLauncher;
import com.resideo.nextgen.reporter.server.DashboardServer;

import java.io.File;
import java.io.IOException;

/**
 * Opt-in "serve immediately after this run" support, shared by the TestNG
 * listener and the Cucumber plugin. Off by default -- a CI build must never
 * hang waiting for someone to Ctrl+C a dashboard server it never asked for.
 * Enable for local runs with {@code -Dresideonextgen.autoServe=true} or the
 * {@code RESIDEONEXTGEN_AUTO_SERVE=true} environment variable.
 */
public final class AutoServeSupport {

    public static final String PROP_AUTO_SERVE = "resideonextgen.autoServe";
    public static final String ENV_AUTO_SERVE = "RESIDEONEXTGEN_AUTO_SERVE";

    private AutoServeSupport() {
    }

    public static boolean isEnabled() {
        String prop = System.getProperty(PROP_AUTO_SERVE);
        if (prop != null) {
            return Boolean.parseBoolean(prop);
        }
        String env = System.getenv(ENV_AUTO_SERVE);
        return env != null && Boolean.parseBoolean(env);
    }

    /**
     * Starts the dashboard server and blocks the calling thread (similar in
     * spirit to {@code mvn jetty:run}) only when explicitly opted in.
     * No-op otherwise.
     */
    public static void maybeAutoServe(File dataDir) {
        if (!isEnabled()) {
            return;
        }
        try {
            DashboardServer server = DashboardServer.start(dataDir, 0);
            System.out.println("[ResideoNextGen] Dashboard is live at " + server.getUrl());
            System.out.println("[ResideoNextGen] Press Ctrl+C to stop.");
            BrowserLauncher.openQuietly(server.getUrl());
            server.blockUntilStopped();
        } catch (IOException e) {
            System.err.println("[ResideoNextGen] Auto-serve failed to start: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
