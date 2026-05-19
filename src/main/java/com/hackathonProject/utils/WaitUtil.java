package com.hackathonProject.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class WaitUtil {

    private static final Logger logger = LogManager.getLogger(WaitUtil.class);

    // Waits for page to fully load
    public static void waitForPageLoad(WebDriver driver) {
        new WebDriverWait(driver, Duration.ofSeconds(20))
            .until(d -> ((JavascriptExecutor) d)
                .executeScript("return document.readyState").equals("complete"));
    }

    // Waits for a @FindBy element to become visible
    public static void waitForElementVisible(WebDriver driver, WebElement element) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOf(element));
        } catch (Exception e) {
            logger.warn("Element not visible within timeout");
        }
    }

    // Waits for an element by locator, returns it or null
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

    // Waits for at least minCount elements to appear
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

    // Short fluent pause without Thread.sleep
    public static void briefPause(WebDriver driver, int millis) {
        try {
            new FluentWait<>(driver)
                .withTimeout(Duration.ofMillis(millis))
                .pollingEvery(Duration.ofMillis(millis))
                .until(d -> false);
        } catch (Exception ignored) {}
    }
}