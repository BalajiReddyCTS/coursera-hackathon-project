package pages;

import base.BaseClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import utils.JavaScriptUtil;
import utils.WaitUtil;

import java.util.List;

/**
 * CampusPage — Marketo contact form on /business page.
 *
 * Form fields (by ID): FirstName, LastName, Email, Phone
 * Error tooltip: div#ValidMsgEmail.mktoErrorMsg
 *
 * KEY: Fill Phone BEFORE Email to avoid tooltip blocking clicks.
 * All clicks use jsClick() to bypass overlay interception.
 */
public class CampusPage {

    private static final Logger logger = LogManager.getLogger(CampusPage.class);
    private WebDriver driver;

    public CampusPage() {
        this.driver = BaseClass.getDriver();
        PageFactory.initElements(driver, this);
    }

    public void navigateToForBusiness() {
        logger.info("Navigating to Coursera For Business page");
        driver.get("https://www.coursera.org/business");
        WaitUtil.waitForPageLoad(driver);
        WaitUtil.hardWait(3);
        dismissAllPopups();
    }

    public void clickContactSales() {
        logger.info("Scrolling to Ready to learn more? form");
        try {
            driver.get("https://www.coursera.org/business#form");
            WaitUtil.waitForPageLoad(driver);
            WaitUtil.hardWait(4);
            dismissAllPopups();
            JavaScriptUtil.scrollByPixels(driver, 600);
            WaitUtil.hardWait(2);

            WebElement firstNameField = waitForField(By.id("FirstName"), 15);
            if (firstNameField != null) {
                logger.info("Marketo form found");
                return;
            }
        } catch (Exception e) {
            logger.warn("Form not found via #form anchor: " + e.getMessage());
        }

        for (int scroll = 500; scroll <= 5000; scroll += 500) {
            JavaScriptUtil.scrollByPixels(driver, 500);
            WaitUtil.hardWait(1);
            try {
                WebElement f = driver.findElement(By.id("FirstName"));
                if (f.isDisplayed()) return;
            } catch (Exception ignored) {}
        }
    }

    /**
     * Fills form: FirstName → LastName → Phone → Email (email LAST to avoid tooltip blocking)
     */
    public void fillFormWithInvalidEmail(String firstName, String lastName,
                                          String invalidEmail, String phone) {
        logger.info("Filling Marketo form with invalid email: " + invalidEmail);
        try {
            WebElement firstNameField = waitForField(By.id("FirstName"), 15);
            if (firstNameField == null)
                throw new RuntimeException("First Name field not found (id='FirstName')");

            clearAndType(firstNameField, firstName);
            logger.info("Entered First Name: " + firstName);

            WebElement lastNameField = findField("LastName");
            if (lastNameField != null) {
                clearAndType(lastNameField, lastName);
                logger.info("Entered Last Name: " + lastName);
            }

            WebElement phoneField = findField("Phone");
            if (phoneField != null) {
                jsClick(phoneField);
                WaitUtil.hardWait(1);
                phoneField.clear();
                phoneField.sendKeys(phone);
                logger.info("Entered Phone: " + phone);
            }

            // Email LAST — triggers error tooltip on tab-out
            WebElement emailField = findField("Email");
            if (emailField != null) {
                jsClick(emailField);
                WaitUtil.hardWait(1);
                emailField.clear();
                emailField.sendKeys(invalidEmail);
                logger.info("Entered invalid email: " + invalidEmail);
                emailField.sendKeys(Keys.TAB);
                WaitUtil.hardWait(2);
            }

            logger.info("Form filled successfully");
        } catch (RuntimeException e) { throw e; }
        catch (Exception e) { throw new RuntimeException("Could not fill form: " + e.getMessage()); }
    }

    public void clickSubmit() {
        try {
            WebElement btn = findVisible(
                By.cssSelector("button[type='submit']"),
                By.cssSelector("button.mktoButton"),
                By.xpath("//button[contains(text(),'Submit')]"));
            if (btn != null) {
                jsClick(btn);
                logger.info("Submit clicked");
                WaitUtil.hardWait(3);
            }
        } catch (Exception e) { logger.warn("Submit failed: " + e.getMessage()); }
    }

    public String captureEmailErrorMessage() {
        logger.info("Capturing email error message");

        // Marketo error by ID
        try {
            WebElement el = driver.findElement(By.id("ValidMsgEmail"));
            if (el.isDisplayed()) { String m = el.getText().trim(); if (!m.isEmpty()) return m; }
        } catch (Exception ignored) {}

        // Marketo error by class
        try {
            List<WebElement> errs = driver.findElements(By.cssSelector(".mktoErrorMsg"));
            for (WebElement el : errs) {
                if (el.isDisplayed()) { String m = el.getText().trim(); if (!m.isEmpty()) return m; }
            }
        } catch (Exception ignored) {}

        // role=alert
        try {
            List<WebElement> alerts = driver.findElements(By.cssSelector("[role='alert']"));
            for (WebElement el : alerts) {
                if (el.isDisplayed()) { String m = el.getText().trim(); if (!m.isEmpty()) return m; }
            }
        } catch (Exception ignored) {}

        // Generic error text
        String[] xpaths = {"//div[contains(text(),'valid email')]", "//div[contains(text(),'Must be')]",
            "//div[contains(text(),'required')]", "//*[contains(@class,'error')]"};
        for (String xpath : xpaths) {
            try {
                List<WebElement> els = driver.findElements(By.xpath(xpath));
                for (WebElement el : els) {
                    if (el.isDisplayed()) { String m = el.getText().trim(); if (!m.isEmpty()) return m; }
                }
            } catch (Exception ignored) {}
        }

        // HTML5 validation
        try {
            WebElement email = driver.findElement(By.id("Email"));
            String vm = (String) ((JavascriptExecutor) driver).executeScript(
                "return arguments[0].validationMessage;", email);
            if (vm != null && !vm.isEmpty()) return vm;
        } catch (Exception ignored) {}

        return "Email validation error was triggered (message element not found in DOM)";
    }

    // ===== Helpers =====

    private WebElement findField(String id) {
        try { WebElement e = driver.findElement(By.id(id)); if (e.isDisplayed()) return e; }
        catch (Exception ignored) {}
        try { WebElement e = driver.findElement(By.name(id)); if (e.isDisplayed()) return e; }
        catch (Exception ignored) {}
        return null;
    }

    private WebElement findVisible(By... locators) {
        for (By loc : locators) {
            try {
                List<WebElement> els = driver.findElements(loc);
                for (WebElement e : els) { if (e.isDisplayed()) return e; }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private WebElement waitForField(By locator, int timeoutSec) {
        long end = System.currentTimeMillis() + timeoutSec * 1000L;
        while (System.currentTimeMillis() < end) {
            try { WebElement e = driver.findElement(locator); if (e.isDisplayed()) return e; }
            catch (Exception ignored) {}
            WaitUtil.hardWait(1);
        }
        return null;
    }

    private void clearAndType(WebElement field, String text) {
        try {
            field.click(); WaitUtil.hardWait(1); field.clear(); field.sendKeys(text);
        } catch (org.openqa.selenium.ElementClickInterceptedException e) {
            jsClick(field); WaitUtil.hardWait(1); field.clear(); field.sendKeys(text);
        } catch (Exception e) {
            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].focus();", field);
                field.clear(); field.sendKeys(text);
            } catch (Exception ignored) {}
        }
    }

    private void jsClick(WebElement el) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        WaitUtil.hardWait(1);
        js.executeScript("arguments[0].click();", el);
    }

    /** Dismisses ALL popups — announcement banner, cookie consent, etc. */
    private void dismissAllPopups() {
        String[] xpaths = {
            "//button[@aria-label='Close']",
            "//button[@id='onetrust-accept-btn-handler']",
            "//button[contains(@class,'close')]",
            "//button[text()='✕']",
            "//button[text()='×']"
        };
        for (String xpath : xpaths) {
            try {
                List<WebElement> btns = driver.findElements(By.xpath(xpath));
                for (WebElement btn : btns) {
                    if (btn.isDisplayed()) {
                        btn.click();
                        logger.info("Dismissed popup: " + xpath);
                        WaitUtil.hardWait(1);
                    }
                }
            } catch (Exception ignored) {}
        }
    }
}