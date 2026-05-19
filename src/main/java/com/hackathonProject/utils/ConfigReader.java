package com.hackathonProject.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {

    private static final Logger logger = LogManager.getLogger(ConfigReader.class);

    private static Properties properties = new Properties();

    // Static block: runs ONCE when ConfigReader class is first used.
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

    public static String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isEmpty()) {
            logger.error("Property not found in config: " + key);
            throw new RuntimeException("Missing property in config.properties: " + key);
        }
        return value.trim();
    }

    public static String getPropertyOrDefault(String key, String defaultValue) {
        String value = properties.getProperty(key);
        if (value == null || value.isEmpty()) {
            logger.warn("Property [" + key + "] not found, using default: " + defaultValue);
            return defaultValue;
        }
        return value.trim();
    }
}
