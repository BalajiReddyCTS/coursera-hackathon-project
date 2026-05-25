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
        if (override != null) return override;
        String sysProp = System.getProperty("browser");
        if (sysProp != null && !sysProp.isEmpty()) return sysProp;
        return ConfigReader.getProperty("browser");
    }

    public static void createDriver() {
        String browser = getCurrentBrowser();
        logger.info("Launching browser: " + browser + " on thread: " + Thread.currentThread().getName());

        WebDriver webDriver;

        if (browser.equalsIgnoreCase("chrome")) {
            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.addArguments("--start-maximized");
            chromeOptions.addArguments("--disable-notifications");
            chromeOptions.addArguments("--no-sandbox");
            chromeOptions.addArguments("--disable-dev-shm-usage");
            WebDriverManager.chromedriver().setup();
            webDriver = new ChromeDriver(chromeOptions);
            logger.info("Chrome browser launched");

        } else if (browser.equalsIgnoreCase("edge")) {
            EdgeOptions edgeOptions = new EdgeOptions();
            edgeOptions.addArguments("--start-maximized");
            edgeOptions.addArguments("--disable-notifications");
            edgeOptions.addArguments("--no-sandbox");
            edgeOptions.addArguments("--disable-dev-shm-usage");
            // Force unique user data dir per thread to prevent session sharing
            String uniqueProfile = System.getProperty("java.io.tmpdir")
                + "edge_profile_" + Thread.currentThread().threadId() + "_" + System.currentTimeMillis();
            edgeOptions.addArguments("--user-data-dir=" + uniqueProfile);
            webDriver = new EdgeDriver(edgeOptions);
            logger.info("Edge browser launched (via Selenium Manager) with unique profile");

        } else if (browser.equalsIgnoreCase("firefox")) {
            WebDriverManager.firefoxdriver().setup();
            webDriver = new FirefoxDriver();
            logger.info("Firefox browser launched");

        } else {
            throw new IllegalArgumentException("Unsupported browser: " + browser);
        }

        webDriver.manage().timeouts().implicitlyWait(
            Duration.ofSeconds(Integer.parseInt(ConfigReader.getProperty("implicitWait")))
        );
        webDriver.manage().timeouts().pageLoadTimeout(
            Duration.ofSeconds(Integer.parseInt(ConfigReader.getProperty("maxWaitTime")))
        );

        driver.set(webDriver);
        logger.info("WebDriver [" + browser + "] created and stored in ThreadLocal. Session: "
            + ((org.openqa.selenium.remote.RemoteWebDriver) webDriver).getSessionId());
    }

    public static WebDriver getDriver() {
        return driver.get();
    }

    public static void removeDriver() {
        if (driver.get() != null) {
            String browser = getCurrentBrowser();
            logger.info("Quitting " + browser + " browser on thread: " + Thread.currentThread().getName());
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