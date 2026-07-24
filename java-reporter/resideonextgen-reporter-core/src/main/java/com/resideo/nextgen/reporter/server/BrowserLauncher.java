package com.resideo.nextgen.reporter.server;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Best-effort helper to open the dashboard URL in the user's default
 * browser right after {@code mvn resideonextgen:serve} or opt-in auto-serve
 * starts the embedded server.
 *
 * <p>Deliberately never throws: headless environments (most CI runners,
 * containers without a desktop session) simply won't support this, and
 * that must never fail a build or hang a process -- the URL is always
 * printed to the console regardless, so this is a convenience, not a
 * requirement.
 */
public final class BrowserLauncher {

    /** Opt-out flag; on by default per the feature request. */
    public static final String PROP_OPEN_BROWSER = "resideonextgen.openBrowser";

    private BrowserLauncher() {
    }

    public static boolean isEnabled() {
        return Boolean.parseBoolean(System.getProperty(PROP_OPEN_BROWSER, "true"));
    }

    public static void openQuietly(String url) {
        openQuietly(url, isEnabled());
    }

    public static void openQuietly(String url, boolean enabled) {
        if (!enabled) {
            return;
        }
        try {
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                return;
            }
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException | UnsupportedOperationException | SecurityException ignored) {
            // Best-effort only -- the URL is already printed to the console either way.
        }
    }
}
