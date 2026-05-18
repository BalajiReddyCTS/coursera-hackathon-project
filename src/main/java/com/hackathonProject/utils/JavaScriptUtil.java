package com.hackathonProject.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class JavaScriptUtil {

    private static final Logger logger = LogManager.getLogger(JavaScriptUtil.class);

    public static void scrollAndClick(WebDriver driver, WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        WaitUtil.briefPause(driver, 500);
        js.executeScript("arguments[0].click();", element);
        logger.info("JavaScript click performed on element");
    }

    public static void scrollToBottom(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
        logger.info("Scrolled to bottom of page");
    }

    public static void scrollToTop(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollTo(0, 0);");
        logger.info("Scrolled to top of page");
    }

    public static void scrollByPixels(WebDriver driver, int pixels) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollBy(0, " + pixels + ");");
        logger.info("Scrolled by " + pixels + " pixels");
    }

    public static void highlightElement(WebDriver driver, WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].style.border='3px solid red'", element);
    }

    public static String getTextViaJS(WebDriver driver, WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        return (String) js.executeScript("return arguments[0].innerText;", element);
    }
}