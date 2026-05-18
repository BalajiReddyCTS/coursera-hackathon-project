package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * ConfigReader - reads key-value pairs from config/config.properties.
 *
 * WHY: Hard-coding values (URLs, browser names, timeouts) in test code is bad.
 * If the URL changes, you'd have to update every file.
 * ConfigReader lets you change them in ONE place: config.properties.
 *
 * USAGE: ConfigReader.getProperty("browser") → "chrome"
 */
public class ConfigReader {

    private static final Logger logger = LogManager.getLogger(ConfigReader.class);

    // Properties object holds all key-value pairs from the file
    private static Properties properties = new Properties();

    // Static block: runs ONCE when ConfigReader class is first used.
    // Loads the properties file into memory.
    static {
        try {
            // Path relative to project root
            String configPath = "src/main/resources/config/config.properties";
            FileInputStream fis = new FileInputStream(configPath);
            properties.load(fis);
            fis.close();
            logger.info("config.properties loaded successfully");
        } catch (IOException e) {
            logger.error("CRITICAL: Cannot load config.properties! Check path.");
            throw new RuntimeException("config.properties not found: " + e.getMessage());
        }
    }

    /**
     * Returns the value for a given key from config.properties.
     *
     * @param key the property name (e.g. "browser", "baseUrl")
     * @return the value (e.g. "chrome", "https://www.coursera.org")
     * @throws RuntimeException if key not found
     */
    public static String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isEmpty()) {
            logger.error("Property not found in config: " + key);
            throw new RuntimeException("Missing property in config.properties: " + key);
        }
        return value.trim();
    }

    /**
     * Returns a property value, or a default if not found.
     * Useful for optional properties.
     */
    public static String getPropertyOrDefault(String key, String defaultValue) {
        String value = properties.getProperty(key);
        if (value == null || value.isEmpty()) {
            logger.warn("Property [" + key + "] not found, using default: " + defaultValue);
            return defaultValue;
        }
        return value.trim();
    }
}
