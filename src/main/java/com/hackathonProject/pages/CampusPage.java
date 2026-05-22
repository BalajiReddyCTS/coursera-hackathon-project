package com.hackathonProject.pages;

import com.hackathonProject.base.BaseClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import com.hackathonProject.utils.JavaScriptUtil;
import com.hackathonProject.utils.WaitUtil;

public class CampusPage {

    private static final Logger logger = LogManager.getLogger(CampusPage.class);
    private WebDriver driver;

    @FindBy(id = "FirstName")
    private WebElement firstNameField;

    @FindBy(id = "LastName")
    private WebElement lastNameField;

    @FindBy(id = "Email")
    private WebElement emailField;

    @FindBy(id = "ValidMsgEmail")
    private WebElement emailErrorMsg;

    @FindBy(css = "button[type='submit']")
    private WebElement submitBtn;

    @FindBy(xpath = "//button[@aria-label='Close']")
    private WebElement closePopupBtn;

    public CampusPage() {
        this.driver = BaseClass.getDriver();
        PageFactory.initElements(driver, this);
    }

    public void navigateToForBusiness() {
        driver.get("https://www.coursera.org/business");
        WaitUtil.waitForPageLoad(driver);
        dismissPopup();
    }

    public void clickContactSales() {
        logger.info("Scrolling to form section");
        JavaScriptUtil.scrollByPixels(driver, 2000);
        WaitUtil.waitForElementVisible(driver, firstNameField);
        logger.info("Marketo form found");
    }

    public void fillFormWithInvalidEmail(String firstName, String lastName,
                                          String invalidEmail) {
        logger.info("Filling form with invalid email: " + invalidEmail);

        clearAndType(firstNameField, firstName);
        logger.info("Entered First Name: " + firstName);

        clearAndType(lastNameField, lastName);
        logger.info("Entered Last Name: " + lastName);


        // Email LAST — triggers Marketo error tooltip on tab-out
        jsClick(emailField);
        emailField.clear();
        emailField.sendKeys(invalidEmail);
        emailField.sendKeys(Keys.TAB);
        WaitUtil.waitForElementVisible(driver, emailErrorMsg);
        logger.info("Entered invalid email: " + invalidEmail);

        logger.info("Form filled successfully");
    }

    public void clickSubmit() {
        jsClick(submitBtn);
        logger.info("Submit clicked");
    }

    public String captureEmailErrorMessage() {
        logger.info("Capturing email error message");
        WaitUtil.waitForElementVisible(driver, emailErrorMsg);
        String msg = emailErrorMsg.getText().trim();
        logger.info("Error: " + msg);
        return msg.isEmpty() ? "Email validation error was triggered" : msg;
    }

    private void clearAndType(WebElement field, String text) {
        jsClick(field);
        field.clear();
        field.sendKeys(text);
    }

    private void jsClick(WebElement el) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        js.executeScript("arguments[0].click();", el);
    }

    private void dismissPopup() {
        try {
            if (closePopupBtn.isDisplayed()) closePopupBtn.click();
        } catch (Exception ignored) {}
    }
}