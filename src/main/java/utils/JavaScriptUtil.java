package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * JavaScriptUtil - executes JavaScript via Selenium's JavascriptExecutor.
 *
 * USE CASES:
 * - Clicking elements that are obscured or not "clickable" by Selenium
 * - Scrolling the page (Selenium can't scroll natively)
 * - Highlighting elements for debugging
 * - Reading element properties not exposed via WebDriver API
 */
public class JavaScriptUtil {

    private static final Logger logger = LogManager.getLogger(JavaScriptUtil.class);

    /**
     * Scrolls element into view AND clicks it via JavaScript.
     * Useful when Selenium's normal click fails due to overlapping elements.
     */
    public static void scrollAndClick(WebDriver driver, WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        WaitUtil.hardWait(1);
        js.executeScript("arguments[0].click();", element);
        logger.info("JavaScript click performed on element");
    }

    /** Scrolls to the bottom of the page — triggers lazy loading of content */
    public static void scrollToBottom(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
        logger.info("Scrolled to bottom of page");
    }

    /** Scrolls back to the top of the page */
    public static void scrollToTop(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollTo(0, 0);");
        logger.info("Scrolled to top of page");
    }

    /**
     * Scrolls to a specific pixel position on the page.
     * Useful for reaching elements in the middle of a long page.
     */
    public static void scrollByPixels(WebDriver driver, int pixels) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollBy(0, " + pixels + ");");
        logger.info("Scrolled by " + pixels + " pixels");
    }

    /**
     * Highlights an element with a red border (debugging aid).
     * Great for visually confirming which element is being interacted with.
     */
    public static void highlightElement(WebDriver driver, WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].style.border='3px solid red'", element);
    }

    /**
     * Returns the inner text of an element via JavaScript.
     * Sometimes getText() returns empty but JS returns the actual value.
     */
    public static String getTextViaJS(WebDriver driver, WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        return (String) js.executeScript("return arguments[0].innerText;", element);
    }
}
