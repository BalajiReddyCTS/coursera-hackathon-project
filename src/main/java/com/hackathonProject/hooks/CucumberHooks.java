package com.hackathonProject.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hackathonProject.base.BaseClass;
import com.hackathonProject.utils.ExtentReportManager;
import com.hackathonProject.utils.ScreenshotUtil;

/**
 * CucumberHooks - runs setup/teardown code before and after each Scenario.
 *
 * @Before → launches browser
 * @After  → takes screenshot on failure, then quits browser
 *
 * Cucumber automatically discovers this class because it's in the
 * "glue" package path configured in TestRunner.
 */
public class CucumberHooks {

    private static final Logger logger = LogManager.getLogger(CucumberHooks.class);

    /**
     * @Before runs ONCE before each Cucumber scenario.
     * Creates a fresh browser session for every test.
     *
     * NOTE: In parallel execution, each thread calls this independently,
     * creating its own browser via ThreadLocal (see BaseClass).
     */
    @Before
    public void setUp(Scenario scenario) {
        logger.info("====== SCENARIO STARTED: " + scenario.getName() + " ======");
        logger.info("Tags: " + scenario.getSourceTagNames());

        // Launch browser (reads 'browser' from config.properties)
        BaseClass.createDriver();

        // Initialise Extent Report test node for this scenario
        ExtentReportManager.createTest(scenario.getName());
        ExtentReportManager.logInfo("Browser launched for scenario: " + scenario.getName());

        logger.info("Browser launched successfully");
    }

    /**
     * @After runs ONCE after each Cucumber scenario (pass or fail).
     * Takes screenshot on failure, then always closes the browser.
     */
    @After
    public void tearDown(Scenario scenario) {
        logger.info("====== SCENARIO FINISHED: " + scenario.getName()
                    + " | Status: " + scenario.getStatus() + " ======");

        // If scenario FAILED → capture screenshot and attach to reports
        if (scenario.isFailed()) {
            logger.warn("Scenario FAILED — capturing screenshot");

            // Capture screenshot as byte array
            byte[] screenshotBytes = ScreenshotUtil.captureScreenshotAsBytes(BaseClass.getDriver());

            // Attach to Cucumber HTML report (appears inline in report)
            if (screenshotBytes != null) {
                scenario.attach(screenshotBytes, "image/png", "Failure Screenshot");
            }

            // Also save to disk (screenshots/ folder)
            String screenshotPath = ScreenshotUtil.captureScreenshot(
                BaseClass.getDriver(), scenario.getName()
            );

            // Log in Extent Report
            ExtentReportManager.logFail("Scenario FAILED: " + scenario.getName());
            ExtentReportManager.attachScreenshot(screenshotPath);

        } else {
            // Pass
            ExtentReportManager.logPass("Scenario PASSED: " + scenario.getName());
        }

        // ALWAYS quit browser and clean up ThreadLocal (even if test failed)
        BaseClass.removeDriver();
        logger.info("Browser closed. Scenario teardown complete.");
    }
}
