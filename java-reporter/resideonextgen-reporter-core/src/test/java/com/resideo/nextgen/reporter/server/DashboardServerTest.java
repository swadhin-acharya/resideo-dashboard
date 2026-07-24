package com.resideo.nextgen.reporter.server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression coverage for a real bug found while testing this against a
 * live browser: the bundled index.html loads its script as
 * {@code <script type="module">}, and browsers silently refuse to execute a
 * module script if the server's Content-Type header for it is wrong --
 * resulting in a blank page with no visible error unless dev tools happen
 * to be open. {@link DashboardServer.BundledAssetHandler#contentTypeFor}
 * must never regress on ".js"/".mjs".
 */
class DashboardServerTest {

    @Test
    void contentTypeIsResolvedCorrectlyByExtension() {
        assertEquals("text/html; charset=utf-8", DashboardServer.BundledAssetHandler.contentTypeFor("/index.html"));
        assertEquals("application/javascript; charset=utf-8",
                DashboardServer.BundledAssetHandler.contentTypeFor("/assets/index-ABC123.js"));
        assertEquals("application/javascript; charset=utf-8",
                DashboardServer.BundledAssetHandler.contentTypeFor("/assets/chunk.mjs"));
        assertEquals("text/css; charset=utf-8",
                DashboardServer.BundledAssetHandler.contentTypeFor("/assets/index-XYZ789.css"));
        assertEquals("image/svg+xml", DashboardServer.BundledAssetHandler.contentTypeFor("/favicon.svg"));
        assertEquals("image/png", DashboardServer.BundledAssetHandler.contentTypeFor("/resideo-favicon.png"));
        assertEquals("application/json; charset=utf-8",
                DashboardServer.BundledAssetHandler.contentTypeFor("/config.json"));
        assertEquals("application/octet-stream",
                DashboardServer.BundledAssetHandler.contentTypeFor("/some/unknown.extension"));
    }

    @Test
    void serverServesBundledIndexWithHtmlContentTypeAndLiveJsonData(@TempDir Path dataDir) throws IOException, InterruptedException {
        Files.writeString(dataDir.resolve("summary.json"), "{\"total\":5}", StandardCharsets.UTF_8);

        DashboardServer server = DashboardServer.start(dataDir.toFile(), 0);
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpResponse<String> indexResponse = client.send(
                    HttpRequest.newBuilder(URI.create(server.getUrl())).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, indexResponse.statusCode());
            assertTrue(indexResponse.headers().firstValue("Content-Type").orElse("").startsWith("text/html"),
                    "expected an HTML content type for the app shell, got: " + indexResponse.headers().firstValue("Content-Type"));

            HttpResponse<String> dataResponse = client.send(
                    HttpRequest.newBuilder(URI.create(server.getUrl() + "data/summary.json")).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, dataResponse.statusCode());
            assertEquals("{\"total\":5}", dataResponse.body());
            assertTrue(dataResponse.headers().firstValue("Content-Type").orElse("").startsWith("application/json"));
        } finally {
            server.stop();
        }
    }
}
