package com.hackathonProject.stepdefinitions;

import io.cucumber.java.en.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.asserts.SoftAssert;

import com.hackathonProject.constants.FrameworkConstants;
import com.hackathonProject.pages.HomePage;
import com.hackathonProject.pages.SearchResultsPage;
import com.hackathonProject.pages.SearchResultsPage.CourseInfo;
import com.hackathonProject.utils.ExtentReportManager;

import java.util.List;

public class CourseSearchSteps {

    private static final Logger logger = LogManager.getLogger(CourseSearchSteps.class);

    private HomePage homePage = new HomePage();
    private SearchResultsPage searchResultsPage = new SearchResultsPage();
    private List<CourseInfo> extractedCourses;
    private SoftAssert softAssert = new SoftAssert();

    @Given("the user is on the Coursera home page")
    public void theUserIsOnCourseraHomePage() {
        logger.info("STEP: Opening Coursera home page");
        ExtentReportManager.logInfo("Opening Coursera home page");
        homePage.openHomePage(FrameworkConstants.BASE_URL);
        logger.info("Home page opened successfully");
    }

    @When("the user searches for {string}")
    public void theUserSearchesFor(String searchTerm) {
        logger.info("STEP: Searching for: " + searchTerm);
        ExtentReportManager.logInfo("Searching for: " + searchTerm);
        homePage.searchFor(searchTerm);
    }

    @Then("the search results page should be displayed")
    public void theSearchResultsPageShouldBeDisplayed() {
        logger.info("STEP: Verifying search results page");
        String url = com.hackathonProject.base.BaseClass.getDriver().getCurrentUrl();
        softAssert.assertTrue(
            url.contains("search") || url.contains("coursera"),
            "Expected search results URL but got: " + url
        );
        ExtentReportManager.logPass("Search results page displayed. URL: " + url);
        logger.info("Search results page verified. URL: " + url);
    }

    @When("the user applies the language filter {string}")
    public void theUserAppliesLanguageFilter(String language) {
        logger.info("STEP: Applying language filter: " + language);
        ExtentReportManager.logInfo("Applying language filter: " + language);
        searchResultsPage.selectLanguageFilter(language);
    }

    @When("the user applies the level filter {string}")
    public void theUserAppliesLevelFilter(String level) {
        logger.info("STEP: Applying level filter: " + level);
        ExtentReportManager.logInfo("Applying level filter: " + level);
        searchResultsPage.selectLevelFilter(level);
    }

    @When("the user extracts the first {int} courses with name, hours and rating")
    public void theUserExtractsFirstNCourses(int count) {
        logger.info("STEP: Extracting " + count + " courses");
        ExtentReportManager.logInfo("Extracting " + count + " courses");

        extractedCourses = searchResultsPage.extractCourses(count);

        for (CourseInfo c : extractedCourses) {
            logger.info("Course: " + c);
            ExtentReportManager.logInfo("  " + c.toString());
        }

        softAssert.assertFalse(
            extractedCourses.isEmpty(),
            "No courses were extracted from the search results!"
        );
        logger.info("Extraction complete. Got " + extractedCourses.size() + " courses.");
    }

    @When("the course data is saved to an Excel file")
    public void theCourseDataIsSavedToExcel() {
        logger.info("STEP: Saving course data to Excel");
        softAssert.assertNotNull(extractedCourses, "Courses list is null");
        softAssert.assertFalse(extractedCourses == null || extractedCourses.isEmpty(), "Courses list is empty");

        if (extractedCourses != null && !extractedCourses.isEmpty()) {
            String outputPath = FrameworkConstants.EXCEL_OUTPUT_PATH;
            searchResultsPage.saveCourseDataToExcel(extractedCourses, outputPath);

            java.io.File excelFile = new java.io.File(outputPath);
            softAssert.assertTrue(excelFile.exists(), "Excel file was not created at: " + outputPath);

            ExtentReportManager.logPass("Excel file saved at: " + outputPath);
            logger.info("Excel saved successfully: " + outputPath);
        }
    }

    @Then("the first course name should not be empty")
    public void theFirstCourseNameShouldNotBeEmpty() {
        logger.info("STEP: Asserting first course name is not empty");
        softAssert.assertFalse(
            extractedCourses == null || extractedCourses.isEmpty(),
            "Courses list is empty"
        );

        if (extractedCourses != null && !extractedCourses.isEmpty()) {
            String firstName = extractedCourses.get(0).name;
            softAssert.assertFalse(
                firstName == null || firstName.trim().isEmpty(),
                "First course name is empty!"
            );
            ExtentReportManager.logPass("First course: " + firstName);
            logger.info("First course name: " + firstName);
        }

        softAssert.assertAll();
    }
}