package com.hackathonProject.base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.hackathonProject.utils.ConfigReader;

import java.time.Duration;

/**
 * BaseClass - Foundation of the entire framework.
 *
 * DRIVER MANAGEMENT:
 * - Chrome: uses WebDriverManager (already cached, works fine)
 * - Edge: uses Selenium's built-in Selenium Manager (no external download needed)
 *   Selenium 4.6+ includes Selenium Manager which auto-detects the browser
 *   and downloads the matching driver. No WebDriverManager.edgedriver().setup() needed.
 * - Firefox: uses WebDriverManager
 *
 * CROSS-BROWSER SUPPORT:
 * - browserOverride (ThreadLocal) lets each thread specify its own browser
 * - If set, it overrides config.properties "browser" value
 */
public class BaseClass {

    private static final Logger logger = LogManager.getLogger(BaseClass.class);

    private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private static ThreadLocal<String> browserOverride = new ThreadLocal<>();

    public static void setBrowserOverride(String browser) {
        browserOverride.set(browser);
        logger.info("Browser override set to: " + browser);
    }

    public static String getCurrentBrowser() {
        String override = browserOverride.get();
        if (override != null && !override.isEmpty()) return override;
        return ConfigReader.getProperty("browser");
    }

    public static void createDriver() {
        String browser = getCurrentBrowser();
        logger.info("Launching browser: " + browser);

        WebDriver webDriver;

        switch (browser.toLowerCase()) {

            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                webDriver = new FirefoxDriver();
                logger.info("Firefox browser launched");
                break;

            case "edge":
                // Use Selenium's built-in driver management (Selenium Manager)
                // No WebDriverManager.edgedriver().setup() needed — Selenium 4.6+
                // auto-detects Edge and downloads msedgedriver automatically
                EdgeOptions edgeOptions = new EdgeOptions();
                edgeOptions.addArguments("--start-maximized");
                edgeOptions.addArguments("--disable-notifications");
                webDriver = new EdgeDriver(edgeOptions);
                logger.info("Edge browser launched (via Selenium Manager)");
                break;

            case "chrome":
            default:
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--start-maximized");
                chromeOptions.addArguments("--disable-notifications");
                WebDriverManager.chromedriver().setup();
                webDriver = new ChromeDriver(chromeOptions);
                logger.info("Chrome browser launched");
                break;
        }

        webDriver.manage().timeouts().implicitlyWait(
            Duration.ofSeconds(Integer.parseInt(ConfigReader.getProperty("implicitWait")))
        );

        driver.set(webDriver);
        logger.info("WebDriver [" + browser + "] created and stored in ThreadLocal");
    }

    public static WebDriver getDriver() {
        return driver.get();
    }

    public static void removeDriver() {
        if (driver.get() != null) {
            String browser = getCurrentBrowser();
            logger.info("Quitting " + browser + " browser and removing from ThreadLocal");
            try {
                driver.get().quit();
            } catch (Exception e) {
                logger.warn("Error quitting browser: " + e.getMessage());
            }
            driver.remove();
            browserOverride.remove();
        }
    }
}