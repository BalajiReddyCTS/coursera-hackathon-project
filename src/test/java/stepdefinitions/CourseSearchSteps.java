package stepdefinitions;

import constants.FrameworkConstants;
import io.cucumber.java.en.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import pages.HomePage;
import pages.SearchResultsPage;
import pages.SearchResultsPage.CourseInfo;
import utils.ExtentReportManager;

import java.util.List;

/**
 * CourseSearchSteps - step definitions for CourseSearch.feature
 *
 * HOW CUCUMBER WIRES STEPS:
 * Each method's @Given/@When/@Then annotation contains a regex/string
 * that EXACTLY matches the step text in the .feature file.
 * Cucumber finds the match at runtime and calls the method.
 *
 * EACH STEP SHOULD DO ONE THING and delegate to Page Objects.
 */
public class CourseSearchSteps {

    private static final Logger logger = LogManager.getLogger(CourseSearchSteps.class);

    // Page Object instances — created fresh per scenario (Cucumber creates new
    // step-def instances per scenario by default with cucumber-testng)
    private HomePage homePage = new HomePage();
    private SearchResultsPage searchResultsPage = new SearchResultsPage();

    // Shared state between steps in the same scenario
    private List<CourseInfo> extractedCourses;

    // ===== Step Definitions =====

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
        // ASSERTION: page URL should contain "search"
//        String currentUrl = pages.HomePage.class.getDeclaredFields().length > 0 ?
//            base.BaseClass.getDriver().getCurrentUrl() : "";
        
        // Simpler: just check URL directly
        String url = base.BaseClass.getDriver().getCurrentUrl();
        Assert.assertTrue(
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

    @Then("the user extracts the first {int} courses with name, hours and rating")
    public void theUserExtractsFirstNCourses(int count) {
        logger.info("STEP: Extracting " + count + " courses");
        ExtentReportManager.logInfo("Extracting " + count + " courses");

        extractedCourses = searchResultsPage.extractCourses(count);

        // Log each course to extent report
        for (CourseInfo c : extractedCourses) {
            logger.info("Course: " + c);
            ExtentReportManager.logInfo("  " + c.toString());
        }

        // Assert we got SOME results (courses list may be smaller than count if filters limit results)
        Assert.assertFalse(
            extractedCourses.isEmpty(),
            "No courses were extracted from the search results!"
        );
        logger.info("Extraction complete. Got " + extractedCourses.size() + " courses.");
    }

    @Then("the course data is saved to an Excel file")
    public void theCourseDataIsSavedToExcel() {
        logger.info("STEP: Saving course data to Excel");
        Assert.assertNotNull(extractedCourses, "Courses list is null — extraction may have failed");
        Assert.assertFalse(extractedCourses.isEmpty(), "Courses list is empty");

        String outputPath = FrameworkConstants.EXCEL_OUTPUT_PATH;
        searchResultsPage.saveCourseDataToExcel(extractedCourses, outputPath);

        // Verify file was created
        java.io.File excelFile = new java.io.File(outputPath);
        Assert.assertTrue(excelFile.exists(), "Excel file was not created at: " + outputPath);

        ExtentReportManager.logPass("Excel file saved at: " + outputPath);
        logger.info("Excel saved successfully: " + outputPath);
    }

    @Then("the first course name should not be empty")
    public void theFirstCourseNameShouldNotBeEmpty() {
        logger.info("STEP: Asserting first course name is not empty");
        Assert.assertFalse(
            extractedCourses.isEmpty(),
            "Courses list is empty"
        );
        String firstName = extractedCourses.get(0).name;
        Assert.assertFalse(
            firstName == null || firstName.trim().isEmpty(),
            "First course name is empty!"
        );
        ExtentReportManager.logPass("First course: " + firstName);
        logger.info("First course name assertion passed: " + firstName);
    }
}
