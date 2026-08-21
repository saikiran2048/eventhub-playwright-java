package org.example.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {

    private static final Logger log = LogManager.getLogger(ConfigManager.class);

    private static Properties properties;

    private ConfigManager() {
    }

    // Called once at suite start via BaseTest/BaseApiTest
    // -Denv=staging -> loads config-staging.properties
    // Default -> config-test.properties if no flag passed
    public static synchronized void loadConfig() {
        if (properties != null) {
            return; // Already loaded
        }

        String env = System.getProperty("env", "test");
        String fileName = "config-" + env + ".properties";

        log.info("Loading config for environment: [{}] -> {}", env, fileName);

        properties = new Properties();

        try (InputStream input = ConfigManager.class
                .getClassLoader()
                .getResourceAsStream(fileName)) {

            if (input == null) {
                throw new RuntimeException(
                        "Config file not found in classpath: " + fileName
                                + " | Valid values: dev, test, staging"
                );
            }

            properties.load(input);
            log.info("Config loaded successfully — base.url = {}",
                    properties.getProperty("base.url"));

        } catch (IOException e) {
            log.error("Failed to load config file: {}", fileName);
            throw new RuntimeException("Config loading failed", e);
        }
    }

    // ─── Getters ───────────────────────────────────────────

    public static String getBaseUrl() {
        return get("base.url");
    }

    public static String getBrowser() {
        return get("browser");
    }

    public static int getDefaultTimeout() {
        return Integer.parseInt(get("default.timeout"));
    }

    public static int getNavigationTimeout() {
        return Integer.parseInt(get("navigation.timeout"));
    }

    public static String getEnvironment() {
        return get("environment");
    }

    public static String getApiBaseUrl() {
        return get("api.base.url");
    }

    public static int getApiTimeout() {
        return Integer.parseInt(get("api.timeout"));
    }

    // "true"/"false" — whether to run headless. Defaults to true so CI runs
    // (GitHub Actions) work out of the box with no flag needed.
    public static boolean isHeadless() {
        return Boolean.parseBoolean(
                System.getProperty("headless", getOptional("headless", "true")));
    }

    // Generic getter
    public static String get(String key) {
        guardLoaded();
        String value = properties.getProperty(key);
        if (value == null) {
            throw new RuntimeException(
                    "Property not found: [" + key + "] "
                            + "in config-" + getEnvironment() + ".properties"
            );
        }
        return value.trim();
    }

    // Non-throwing getter for optional properties
    public static String getOptional(String key, String defaultValue) {
        guardLoaded();
        String value = properties.getProperty(key);
        return (value == null || value.trim().isEmpty()) ? defaultValue : value.trim();
    }

    private static void guardLoaded() {
        if (properties == null) {
            throw new RuntimeException(
                    "ConfigManager not initialized. "
                            + "Call ConfigManager.loadConfig() in BaseTest @BeforeSuite"
            );
        }
    }
}
