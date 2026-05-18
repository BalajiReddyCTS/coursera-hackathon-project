package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * WaitUtil - smart waits for web automation.
 *
 * WHY WAITS MATTER:
 * Web pages load asynchronously. If Selenium tries to click a button before
 * it's visible, the test fails. Waits tell Selenium: "keep checking
 * until this condition is true, or until timeout."
 *
 * TYPES:
 * - Implicit wait: set once, applies to every findElement call
 * - Explicit wait (WebDriverWait): used for specific conditions
 * - Hard wait (Thread.sleep): last resort — avoid in production
 */
public class WaitUtil {

    private static final Logger logger = LogManager.getLogger(WaitUtil.class);
    private static final int DEFAULT_TIMEOUT = 15; // seconds

    /** Waits until element is VISIBLE in viewport */
    public static void waitForElementVisible(WebDriver driver, WebElement element) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
            wait.until(ExpectedConditions.visibilityOf(element));
        } catch (Exception e) {
            logger.warn("Element not visible within timeout: " + e.getMessage());
        }
    }

    /** Waits until element is CLICKABLE (visible + enabled) */
    public static void waitForElementClickable(WebDriver driver, WebElement element) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
            wait.until(ExpectedConditions.elementToBeClickable(element));
        } catch (Exception e) {
            logger.warn("Element not clickable within timeout: " + e.getMessage());
        }
    }

    /** Waits for browser page to fully load (document.readyState == 'complete') */
    public static void waitForPageLoad(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            wait.until(d -> ((JavascriptExecutor) d)
                .executeScript("return document.readyState").equals("complete"));
        } catch (Exception e) {
            logger.warn("Page load wait timed out: " + e.getMessage());
        }
    }

    /**
     * Hard wait — use ONLY when necessary (e.g. waiting for animations).
     * Thread.sleep blocks the thread; prefer explicit waits when possible.
     */
    public static void hardWait(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("hardWait interrupted");
        }
    }
}
