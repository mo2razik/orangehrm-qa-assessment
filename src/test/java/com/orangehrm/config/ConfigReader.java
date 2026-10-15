package com.orangehrm.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Central access point for configuration values.
 * <p>
 * Resolution order (first match wins):
 * <ol>
 *     <li>JVM system property (-Dkey=value)</li>
 *     <li>Environment variable (key upper-cased, dots replaced by underscores, e.g. APP_PASSWORD)</li>
 *     <li>config/config.properties on the test classpath</li>
 * </ol>
 */
public final class ConfigReader {

    private static final String CONFIG_FILE = "config/config.properties";
    private static final Properties PROPERTIES = load();

    private ConfigReader() {
    }

    private static Properties load() {
        Properties properties = new Properties();
        try (InputStream in = ConfigReader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                throw new IllegalStateException("Configuration file not found on classpath: " + CONFIG_FILE);
            }
            properties.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read " + CONFIG_FILE, e);
        }
        return properties;
    }

    public static String get(String key) {
        String value = System.getProperty(key);
        if (isBlank(value)) {
            value = System.getenv(key.toUpperCase().replace('.', '_'));
        }
        if (isBlank(value)) {
            value = PROPERTIES.getProperty(key);
        }
        if (isBlank(value)) {
            throw new IllegalStateException("Missing configuration value for key: " + key);
        }
        return value.trim();
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    // ----- Typed accessors for frequently used keys -----

    public static String baseUrl() {
        return get("base.url");
    }

    public static String username() {
        String fromEnv = System.getenv("ORANGEHRM_USERNAME");
        return isBlank(System.getProperty("app.username")) && !isBlank(fromEnv) ? fromEnv : get("app.username");
    }

    public static String password() {
        String fromEnv = System.getenv("ORANGEHRM_PASSWORD");
        return isBlank(System.getProperty("app.password")) && !isBlank(fromEnv) ? fromEnv : get("app.password");
    }

    public static String browser() {
        return get("browser");
    }

    public static boolean headless() {
        return getBoolean("headless");
    }

    public static int explicitTimeout() {
        return getInt("timeout.explicit");
    }
}
