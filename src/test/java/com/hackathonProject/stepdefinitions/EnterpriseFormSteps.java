package com.hackathonProject.stepdefinitions;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.asserts.SoftAssert;

import com.hackathonProject.pages.CampusPage;
import com.hackathonProject.utils.ExtentReportManager;
import com.hackathonProject.utils.ScreenshotUtil;

import java.util.List;
import java.util.Map;

public class EnterpriseFormSteps {

    private static final Logger logger = LogManager.getLogger(EnterpriseFormSteps.class);

    private CampusPage campusPage = new CampusPage();
    private String capturedErrorMessage;
    private SoftAssert softAssert = new SoftAssert();

    @Given("the user navigates to the Coursera For Business page")
    public void theUserNavigatesToForBusiness() {
        logger.info("STEP: Navigating to Coursera For Business page");
        ExtentReportManager.logInfo("Opening Coursera For Business page");
        campusPage.navigateToForBusiness();
    }

    @When("the user clicks on Contact Sales")
    public void theUserClicksContactSales() {
        logger.info("STEP: Clicking Contact Sales / scrolling to form");
        ExtentReportManager.logInfo("Clicking Contact Sales button");
        campusPage.clickContactSales();
    }

    @When("the user fills the contact form with the following details:")
    public void theUserFillsContactForm(DataTable dataTable) {
        logger.info("STEP: Filling contact form with DataTable values");

        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        Map<String, String> formData = rows.get(0);

        String firstName = formData.get("firstName");
        String lastName  = formData.get("lastName");
        String email     = formData.get("email");

        logger.info("Form data: " + formData);
        ExtentReportManager.logInfo("Filling form: " + formData);

        campusPage.fillFormWithInvalidEmail(firstName, lastName, email);

        String screenshotPath = ScreenshotUtil.captureScreenshot(
            com.hackathonProject.base.BaseClass.getDriver(), "FormFilledWithInvalidEmail"
        );
        ExtentReportManager.attachScreenshot(screenshotPath);
    }

    @Then("an email validation error message should be displayed")
    public void anEmailValidationErrorShouldBeDisplayed() {
        logger.info("STEP: Checking for email validation error message");

        campusPage.clickSubmit();
        capturedErrorMessage = campusPage.captureEmailErrorMessage();

        logger.info("Captured error message: " + capturedErrorMessage);
        ExtentReportManager.logInfo("Error message captured: " + capturedErrorMessage);

        String screenshotPath = ScreenshotUtil.captureScreenshot(
            com.hackathonProject.base.BaseClass.getDriver(), "EmailValidationError"
        );
        ExtentReportManager.attachScreenshot(screenshotPath);

        softAssert.assertNotNull(capturedErrorMessage, "Error message is null");
        softAssert.assertFalse(
            capturedErrorMessage == null || capturedErrorMessage.trim().isEmpty(),
            "No error message was captured for invalid email"
        );
    }

    @Then("the error message should contain {string}")
    public void theErrorMessageShouldContain(String expectedText) {
        logger.info("STEP: Asserting error message contains: " + expectedText);

        softAssert.assertNotNull(capturedErrorMessage, "Error message is null");

        if (capturedErrorMessage != null) {
            boolean matches = capturedErrorMessage.toLowerCase().contains(expectedText.toLowerCase());
            
            if (matches) {
                ExtentReportManager.logPass("Error contains '" + expectedText + "': " + capturedErrorMessage);
                logger.info("Assertion PASSED: " + capturedErrorMessage);
            } else {
                ExtentReportManager.logFail("Expected '" + expectedText + "' but got: " + capturedErrorMessage);
                logger.error("Assertion FAILED. Expected: " + expectedText + " | Actual: " + capturedErrorMessage);
            }

            softAssert.assertTrue(matches,
                "Expected error to contain '" + expectedText + "' but got: " + capturedErrorMessage);
        }

        softAssert.assertAll();
    }
}