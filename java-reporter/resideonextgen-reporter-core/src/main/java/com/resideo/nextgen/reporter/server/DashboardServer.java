package com.resideo.nextgen.reporter.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

/**
 * Zero-dependency embedded HTTP server (the JDK's built-in HttpServer) that
 * serves the pre-built ResideoNextGen React dashboard, bundled as classpath
 * resources under {@code dashboard-dist/}, with {@code /data/*} served live
 * from disk so it always reflects the most recently generated report --
 * no rebuild of the bundle is ever needed between test runs.
 *
 * This never serves Allure's own HTML report; it only serves our bundled
 * dashboard shell plus the canonical JSON {@link com.resideo.nextgen.reporter.engine.ReportEngine} wrote.
 */
public class DashboardServer {

    private static final String BUNDLE_ROOT = "dashboard-dist";

    private final HttpServer server;
    private final int port;
    private final CountDownLatch stopLatch = new CountDownLatch(1);

    private DashboardServer(HttpServer server, int port) {
        this.server = server;
        this.port = port;
    }

    public static DashboardServer start(File dataDir, int preferredPort) throws IOException {
        int port = Math.max(preferredPort, 0);
        HttpServer httpServer = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        httpServer.setExecutor(Executors.newCachedThreadPool(runnable -> {
            Thread t = new Thread(runnable, "resideonextgen-dashboard-server");
            t.setDaemon(true);
            return t;
        }));

        httpServer.createContext("/data", new LiveDataHandler(dataDir));
        httpServer.createContext("/", new BundledAssetHandler());

        httpServer.start();
        return new DashboardServer(httpServer, httpServer.getAddress().getPort());
    }

    public String getUrl() {
        return "http://localhost:" + port + "/";
    }

    public int getPort() {
        return port;
    }

    public void stop() {
        server.stop(0);
        stopLatch.countDown();
    }

    /** Blocks the calling thread until {@link #stop()} is called or the JVM is interrupted (Ctrl+C). */
    public void blockUntilStopped() throws InterruptedException {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
        stopLatch.await();
    }

    private static class LiveDataHandler implements HttpHandler {
        private final File dataDir;

        LiveDataHandler(File dataDir) {
            this.dataDir = dataDir;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath(); // e.g. /data/summary.json
            String relative = path.substring("/data".length());
            if (relative.startsWith("/")) {
                relative = relative.substring(1);
            }
            if (relative.isBlank()) {
                respond(exchange, 404, "text/plain", "Not found".getBytes());
                return;
            }
            File file = new File(dataDir, relative).getCanonicalFile();
            File canonicalRoot = dataDir.getCanonicalFile();
            if (!file.getPath().startsWith(canonicalRoot.getPath()) || !file.isFile()) {
                respond(exchange, 404, "text/plain", "Not found".getBytes());
                return;
            }
            byte[] bytes = Files.readAllBytes(file.toPath());
            respond(exchange, 200, "application/json", bytes);
        }
    }

    static class BundledAssetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/") || path.isBlank()) {
                path = "/index.html";
            }
            String resourcePath = BUNDLE_ROOT + path;

            byte[] bytes = readResource(resourcePath);
            if (bytes == null) {
                // SPA fallback: any unknown path serves index.html. HashRouter
                // means the browser never actually asks the server for a
                // client-side route, but this keeps direct hits harmless.
                bytes = readResource(BUNDLE_ROOT + "/index.html");
                if (bytes == null) {
                    respond(exchange, 404, "text/plain", "ResideoNextGen Dashboard bundle not found".getBytes());
                    return;
                }
            }
            respond(exchange, 200, contentTypeFor(path), bytes);
        }

        /**
         * Resolves content type purely from the file extension. Deliberately
         * does NOT use {@code java.net.URLConnection#guessContentTypeFromName}:
         * it is unreliable across JDK/OS combinations for ".js" (sometimes null,
         * sometimes an incorrect type), and the bundled index.html loads its
         * script as {@code <script type="module">} -- browsers strictly
         * enforce the MIME type for module scripts and will silently refuse
         * to execute (blank page, no visible error without opening dev
         * tools) if the server sends the wrong Content-Type.
         */
        static String contentTypeFor(String path) {
            String lower = path.toLowerCase(java.util.Locale.ROOT);
            if (lower.endsWith(".html")) {
                return "text/html; charset=utf-8";
            } else if (lower.endsWith(".js") || lower.endsWith(".mjs")) {
                return "application/javascript; charset=utf-8";
            } else if (lower.endsWith(".css")) {
                return "text/css; charset=utf-8";
            } else if (lower.endsWith(".json")) {
                return "application/json; charset=utf-8";
            } else if (lower.endsWith(".svg")) {
                return "image/svg+xml";
            } else if (lower.endsWith(".png")) {
                return "image/png";
            } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                return "image/jpeg";
            } else if (lower.endsWith(".ico")) {
                return "image/x-icon";
            } else if (lower.endsWith(".woff2")) {
                return "font/woff2";
            } else if (lower.endsWith(".woff")) {
                return "font/woff";
            } else if (lower.endsWith(".ttf")) {
                return "font/ttf";
            }
            return "application/octet-stream";
        }

        private byte[] readResource(String resourcePath) throws IOException {
            try (InputStream in = DashboardServer.class.getClassLoader().getResourceAsStream(resourcePath)) {
                if (in == null) {
                    return null;
                }
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                in.transferTo(out);
                return out.toByteArray();
            }
        }
    }

    private static void respond(HttpExchange exchange, int status, String contentType, byte[] body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }
}
