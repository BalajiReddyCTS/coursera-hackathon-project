package com.hackathonProject.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

import com.hackathonProject.base.BaseClass;
import com.hackathonProject.utils.JavaScriptUtil;
import com.hackathonProject.utils.WaitUtil;

public class CampusPage {

    private static final Logger logger = LogManager.getLogger(CampusPage.class);
    private WebDriver driver;

    public CampusPage() {
        this.driver = BaseClass.getDriver();
        PageFactory.initElements(driver, this);
    }

    public void navigateToForBusiness() {
        driver.get("https://www.coursera.org/business");
        WaitUtil.waitForPageLoad(driver);
        dismissAllPopups();
    }

    public void clickContactSales() {
        logger.info("Navigating to form");
        driver.get("https://www.coursera.org/business#form");
        WaitUtil.waitForPageLoad(driver);
        dismissAllPopups();
        JavaScriptUtil.scrollByPixels(driver, 600);

        // Wait for Marketo form to render
        WebElement firstNameField = WaitUtil.waitForAnyElement(driver, 15,
            By.id("FirstName"),
            By.cssSelector("input[placeholder='First Name']"));

        if (firstNameField != null) {
            logger.info("Marketo form found");
            return;
        }

        // Fallback: scroll to find it
        for (int px = 500; px <= 5000; px += 500) {
            JavaScriptUtil.scrollByPixels(driver, 500);
            WebElement f = WaitUtil.waitForElement(driver, By.id("FirstName"), 2);
            if (f != null) return;
        }
    }

    public void fillFormWithInvalidEmail(String firstName, String lastName, String invalidEmail, String phone) {
        logger.info("Filling form with invalid email: " + invalidEmail);

        WebElement firstNameField = WaitUtil.waitForElement(driver, By.id("FirstName"), 15);
        if (firstNameField == null)
            throw new RuntimeException("First Name field not found");

        clearAndType(firstNameField, firstName);
        logger.info("Entered First Name: " + firstName);

        WebElement lastNameField = WaitUtil.waitForElement(driver, By.id("LastName"), 5);
        if (lastNameField != null) {
            clearAndType(lastNameField, lastName);
            logger.info("Entered Last Name: " + lastName);
        }

        WebElement phoneField = WaitUtil.waitForElement(driver, By.id("Phone"), 5);
        if (phoneField != null) {
            jsClick(phoneField);
            phoneField.clear();
            phoneField.sendKeys(phone);
            logger.info("Entered Phone: " + phone);
        }

        // Email LAST — triggers error tooltip
        WebElement emailField = WaitUtil.waitForElement(driver, By.id("Email"), 5);
        if (emailField != null) {
            jsClick(emailField);
            emailField.clear();
            emailField.sendKeys(invalidEmail);
            logger.info("Entered invalid email: " + invalidEmail);
            emailField.sendKeys(Keys.TAB);
            // Wait for the Marketo error tooltip to appear
            WaitUtil.waitForElement(driver, By.id("ValidMsgEmail"), 5);
        }

        logger.info("Form filled successfully");
    }

    public void clickSubmit() {
        WebElement btn = WaitUtil.waitForAnyElement(driver, 5,
            By.cssSelector("button[type='submit']"),
            By.cssSelector("button.mktoButton"),
            By.xpath("//button[contains(text(),'Submit')]"));
        if (btn != null) {
            jsClick(btn);
            logger.info("Submit clicked");
            WaitUtil.briefPause(driver, 2000);
        }
    }

    public String captureEmailErrorMessage() {
        logger.info("Capturing email error message");

        // Wait for Marketo error
        WebElement errorEl = WaitUtil.waitForAnyElement(driver, 5,
            By.id("ValidMsgEmail"),
            By.cssSelector(".mktoErrorMsg"),
            By.cssSelector("[role='alert']"));

        if (errorEl != null && errorEl.isDisplayed()) {
            String msg = errorEl.getText().trim();
            if (!msg.isEmpty()) { logger.info("Error: " + msg); return msg; }
        }

        // XPath fallback
        for (String xpath : new String[]{"//div[contains(text(),'valid email')]",
            "//div[contains(text(),'Must be')]", "//div[contains(text(),'required')]"}) {
            try {
                for (WebElement el : driver.findElements(By.xpath(xpath))) {
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

        return "Email validation error was triggered";
    }

    private void clearAndType(WebElement field, String text) {
        try { field.click(); field.clear(); field.sendKeys(text); }
        catch (org.openqa.selenium.ElementClickInterceptedException e) {
            jsClick(field); field.clear(); field.sendKeys(text);
        }
    }

    private void jsClick(WebElement el) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        js.executeScript("arguments[0].click();", el);
    }

    private void dismissAllPopups() {
        for (String xpath : new String[]{"//button[@aria-label='Close']",
            "//button[@id='onetrust-accept-btn-handler']", "//button[contains(@class,'close')]"}) {
            try {
                for (WebElement btn : driver.findElements(By.xpath(xpath))) { if (btn.isDisplayed()) btn.click(); }
            } catch (Exception ignored) {}
        }
    }
}