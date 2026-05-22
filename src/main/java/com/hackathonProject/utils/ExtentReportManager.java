package com.hackathonProject.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ExtentReportManager {

    private static final Logger logger = LogManager.getLogger(ExtentReportManager.class);
    private static final String TIMESTAMP = LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

    private static ExtentReports extentReports;
    private static ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    // Exposed so CucumberListener can archive cucumber reports to the same folder
    public static String getTimestamp() {
        return TIMESTAMP;
    }

    private static synchronized ExtentReports getExtentReports() {
        if (extentReports == null) {
            String reportPath = "reports/extent/ExtentReport_" + TIMESTAMP + ".html";

            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
            sparkReporter.config().setDocumentTitle("Coursera Automation Report");
            sparkReporter.config().setReportName("BDD Test Execution Report");
            sparkReporter.config().setTheme(Theme.DARK);
            sparkReporter.config().setEncoding("UTF-8");

            extentReports = new ExtentReports();
            extentReports.attachReporter(sparkReporter);

            extentReports.setSystemInfo("Framework", "Cucumber BDD + Selenium");
            extentReports.setSystemInfo("Browser", ConfigReader.getPropertyOrDefault("browser", "chrome"));
            extentReports.setSystemInfo("OS", System.getProperty("os.name"));
            extentReports.setSystemInfo("Java Version", System.getProperty("java.version"));

            logger.info("ExtentReports initialised at: " + reportPath);
        }
        return extentReports;
    }

    public static void createTest(String testName) {
        ExtentTest test = getExtentReports().createTest(testName);
        extentTest.set(test);
    }

    public static void logPass(String message) {
        if (extentTest.get() != null) extentTest.get().pass(message);
    }

    public static void logFail(String message) {
        if (extentTest.get() != null) extentTest.get().fail(message);
    }

    public static void logInfo(String message) {
        if (extentTest.get() != null) extentTest.get().info(message);
    }

    public static void logWarning(String message) {
        if (extentTest.get() != null) extentTest.get().warning(message);
    }

    public static void attachScreenshot(String screenshotPath) {
        if (extentTest.get() != null && screenshotPath != null && !screenshotPath.isEmpty()) {
            try {
                extentTest.get().addScreenCaptureFromPath(screenshotPath, "Screenshot");
            } catch (Exception e) {
                logger.warn("Could not attach screenshot: " + e.getMessage());
            }
        }
    }

    public static synchronized void flushReports() {
        if (extentReports != null) {
            extentReports.flush();
            logger.info("Extent Reports flushed to disk");
        }
    }
}