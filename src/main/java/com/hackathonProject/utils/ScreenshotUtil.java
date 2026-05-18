package com.hackathonProject.utils;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ScreenshotUtil - captures screenshots of the browser.
 *
 * WHEN USED:
 * - In CucumberHooks @After when a scenario fails
 * - Optionally in step definitions for key steps
 * - Screenshots are saved to screenshots/ folder with timestamp in name
 * - Bytes version attaches inline to Cucumber HTML report
 */
public class ScreenshotUtil {

    private static final Logger logger = LogManager.getLogger(ScreenshotUtil.class);
    private static final String SCREENSHOT_DIR = "screenshots/";

    /**
     * Captures screenshot and saves to disk.
     *
     * @param driver       current WebDriver
     * @param scenarioName used as part of the file name
     * @return absolute path to the saved screenshot
     */
    public static String captureScreenshot(WebDriver driver, String scenarioName) {
        try {
            // TakesScreenshot is implemented by all WebDriver implementations
            TakesScreenshot ts = (TakesScreenshot) driver;
            File srcFile = ts.getScreenshotAs(OutputType.FILE);

            // Create a unique filename: ScenarioName_2024-01-15_14-30-45.png
            String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
//            String safeName = scenarioName.replaceAll("[^a-zA-Z0-9_]", "_");
            String fileName = SCREENSHOT_DIR + scenarioName + "_" + timestamp + ".png";

            // Create directory if it doesn't exist
            File destDir = new File(SCREENSHOT_DIR);
            if (!destDir.exists()) destDir.mkdirs();

            File destFile = new File(fileName);
            FileUtils.copyFile(srcFile, destFile);

            logger.info("Screenshot saved: " + destFile.getAbsolutePath());
            return destFile.getAbsolutePath();

        } catch (IOException e) {
            logger.error("Screenshot save failed: " + e.getMessage());
            return "";
        }
    }

    /**
     * Captures screenshot as byte array.
     * Used by Cucumber to embed screenshot directly in HTML report.
     *
     * @return PNG bytes, or null if capture failed
     */
    public static byte[] captureScreenshotAsBytes(WebDriver driver) {
        try {
            TakesScreenshot ts = (TakesScreenshot) driver;
            return ts.getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            logger.error("Screenshot capture (bytes) failed: " + e.getMessage());
            return null;
        }
    }
}
