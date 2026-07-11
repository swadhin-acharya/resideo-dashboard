package com.sample.config;

import java.io.InputStream;
import java.util.Properties;

public class Config {

    private static final Properties props = new Properties();

    static {
        try (InputStream is = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (Exception e) {
            System.err.println("[Config] No config.properties found, using env/sysprops only");
        }
        props.putAll(System.getProperties());
    }

    public static String get(String key) {
        String val = System.getenv(toEnvKey(key));
        if (val != null && !val.isEmpty()) return val;
        return props.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        String val = get(key);
        return val != null ? val : defaultValue;
    }

    public static int getInt(String key, int defaultValue) {
        String val = get(key);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String val = get(key);
        if (val == null) return defaultValue;
        return Boolean.parseBoolean(val);
    }

    private static String toEnvKey(String key) {
        return key.replace('.', '_').replace('-', '_').toUpperCase();
    }

    public static class App {
        public static String baseUrl() {
            return get("app.base.url", "https://app.resideo.com");
        }
        public static String username() {
            return get("app.username", "demo@resideo.com");
        }
        public static String password() {
            return get("app.password", "demo1234");
        }
        public static int implicitWait() {
            return getInt("app.implicit.wait.seconds", 10);
        }
    }

    public static class Browser {
        public static String name() {
            return get("browser.name", "chrome");
        }
        public static boolean headless() {
            return getBoolean("browser.headless", false);
        }
        public static int timeout() {
            return getInt("browser.timeout.seconds", 30);
        }
    }

    public static class Mqtt {
        public static String brokerUrl() {
            return get("mqtt.broker.url", "tcp://localhost:1883");
        }
        public static String clientId() {
            return get("mqtt.client.id", "denali-framework");
        }
        public static String username() {
            return get("mqtt.username", "");
        }
        public static String password() {
            return get("mqtt.password", "");
        }
    }

    public static class Dashboard {
        public static String url() {
            return get("RESIDEO_DASHBOARD_URL", get("resideo.dashboard.url", "http://localhost:8080"));
        }
        public static String apiToken() {
            return get("RESIDEO_API_TOKEN", get("resideo.api.token", null));
        }
        public static String projectId() {
            return get("RESIDEO_PROJECT_ID", get("resideo.project.id", null));
        }
    }
}
