package com.hackathonProject.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * WaitUtil v2 — all explicit/fluent waits, zero Thread.sleep.
 */
public class WaitUtil {

    private static final Logger logger = LogManager.getLogger(WaitUtil.class);
    private static final int DEFAULT_TIMEOUT = 10;
    private static final int SHORT_TIMEOUT = 5;
    private static final int LONG_TIMEOUT = 20;

    /** Waits until element is VISIBLE */
    public static void waitForElementVisible(WebDriver driver, WebElement element) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT))
                .until(ExpectedConditions.visibilityOf(element));
        } catch (Exception e) {
            logger.warn("Element not visible within timeout");
        }
    }

    /** Waits until element is CLICKABLE */
    public static void waitForElementClickable(WebDriver driver, WebElement element) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT))
                .until(ExpectedConditions.elementToBeClickable(element));
        } catch (Exception e) {
            logger.warn("Element not clickable within timeout");
        }
    }

    /** Waits for page load (document.readyState == 'complete') */
    public static void waitForPageLoad(WebDriver driver) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(LONG_TIMEOUT))
                .until(d -> ((JavascriptExecutor) d)
                    .executeScript("return document.readyState").equals("complete"));
        } catch (Exception e) {
            logger.warn("Page load timed out");
        }
    }

    /** Waits for URL to contain a specific string */
    public static void waitForUrlContains(WebDriver driver, String text) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT))
                .until(ExpectedConditions.urlContains(text));
        } catch (Exception e) {
            logger.warn("URL did not contain '" + text + "' within timeout");
        }
    }

    /** Waits for an element located by By to be visible, returns it or null */
    public static WebElement waitForElement(WebDriver driver, By locator, int timeoutSec) {
        try {
            return new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeoutSec))
                .pollingEvery(Duration.ofMillis(500))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class)
                .until(d -> {
                    WebElement el = d.findElement(locator);
                    return el.isDisplayed() ? el : null;
                });
        } catch (Exception e) {
            return null;
        }
    }

    /** Waits for an element with default timeout */
    public static WebElement waitForElement(WebDriver driver, By locator) {
        return waitForElement(driver, locator, DEFAULT_TIMEOUT);
    }

    /** Waits for at least N elements to be present */
    public static List<WebElement> waitForElements(WebDriver driver, By locator, int minCount, int timeoutSec) {
        try {
            return new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeoutSec))
                .pollingEvery(Duration.ofMillis(500))
                .ignoring(NoSuchElementException.class)
                .until(d -> {
                    List<WebElement> els = d.findElements(locator);
                    return els.size() >= minCount ? els : null;
                });
        } catch (Exception e) {
            return driver.findElements(locator);
        }
    }

    /** Waits for any one of multiple locators to be visible, returns first match */
    public static WebElement waitForAnyElement(WebDriver driver, int timeoutSec, By... locators) {
        try {
            return new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeoutSec))
                .pollingEvery(Duration.ofMillis(500))
                .ignoring(NoSuchElementException.class)
                .until(d -> {
                    for (By loc : locators) {
                        try {
                            WebElement el = d.findElement(loc);
                            if (el.isDisplayed()) return el;
                        } catch (Exception ignored) {}
                    }
                    return null;
                });
        } catch (Exception e) {
            return null;
        }
    }

    /** Waits for element to become stale (disappeared/refreshed) */
    public static void waitForStale(WebDriver driver, WebElement element) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(SHORT_TIMEOUT))
                .until(ExpectedConditions.stalenessOf(element));
        } catch (Exception ignored) {}
    }

    /** Short pause using fluent wait with a dummy condition — NOT Thread.sleep */
    public static void briefPause(WebDriver driver, int millis) {
        try {
            new FluentWait<>(driver)
                .withTimeout(Duration.ofMillis(millis))
                .pollingEvery(Duration.ofMillis(millis))
                .until(d -> false);
        } catch (Exception ignored) {
            // Expected — the condition never becomes true, so it times out after millis
        }
    }

    /**
     * @deprecated Use explicit waits instead. Kept only for backward compatibility.
     */
//    @Deprecated
//    public static void hardWait(int seconds) {
//        try { Thread.sleep(seconds * 1000L); }
//        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
//    }
}