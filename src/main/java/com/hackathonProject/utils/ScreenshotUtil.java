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

public class ScreenshotUtil {

    private static final Logger logger = LogManager.getLogger(ScreenshotUtil.class);
    private static final String SCREENSHOT_DIR = "screenshots/";

    public static String captureScreenshot(WebDriver driver, String scenarioName) {
        try {
            TakesScreenshot ts = (TakesScreenshot) driver;
            File srcFile = ts.getScreenshotAs(OutputType.FILE);

            String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

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
