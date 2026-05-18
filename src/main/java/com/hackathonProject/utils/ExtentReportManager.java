package com.hackathonProject.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ExtentReportManager - manages the Extent Reports HTML report.
 *
 * EXTENT REPORTS CONCEPTS:
 * - ExtentReports    = the overall report document
 * - ExtentSparkReporter = the HTML renderer
 * - ExtentTest       = one test entry in the report (one per Scenario)
 *
 * THREAD SAFETY:
 * ExtentTest is stored in ThreadLocal so parallel tests each write
 * to their own test node in the report without interference.
 *
 * WHEN TO FLUSH:
 * Must call flushReports() at the end of the run to write the file to disk.
 * This is called from CucumberListener.onRunFinished().
 */
public class ExtentReportManager {

    private static final Logger logger = LogManager.getLogger(ExtentReportManager.class);

    // The main report object (shared across threads — thread-safe by design)
    private static ExtentReports extentReports;

    // Each thread gets its own ExtentTest node
    private static ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    // ===== Initialisation (called lazily) =====

    private static synchronized ExtentReports getExtentReports() {
        if (extentReports == null) {
            // Report output path
            String reportPath = "reports/extent/ExtentReport.html";

            // Configure the HTML renderer
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
            sparkReporter.config().setDocumentTitle("Coursera Automation Report");
            sparkReporter.config().setReportName("BDD Test Execution Report");
            sparkReporter.config().setTheme(Theme.DARK); // Dark theme looks professional
            sparkReporter.config().setEncoding("UTF-8");

            // Attach renderer to the report
            extentReports = new ExtentReports();
            extentReports.attachReporter(sparkReporter);

            // System info shown in the report header
            extentReports.setSystemInfo("Framework", "Cucumber BDD + Selenium");
            extentReports.setSystemInfo("Language", "Java 11");
            extentReports.setSystemInfo("Browser", ConfigReader.getPropertyOrDefault("browser", "chrome"));
            extentReports.setSystemInfo("OS", System.getProperty("os.name"));
            extentReports.setSystemInfo("Java Version", System.getProperty("java.version"));

            logger.info("ExtentReports initialised at: " + reportPath);
        }
        return extentReports;
    }

    // ===== Public API =====

    /** Creates a new test node for a Scenario. Call in @Before hook. */
    public static void createTest(String testName) {
        ExtentTest test = getExtentReports().createTest(testName);
        extentTest.set(test); // Store in this thread's slot
    }

    /** Logs a PASS result */
    public static void logPass(String message) {
        if (extentTest.get() != null) {
            extentTest.get().pass(message);
        }
    }

    /** Logs a FAIL result */
    public static void logFail(String message) {
        if (extentTest.get() != null) {
            extentTest.get().fail(message);
        }
    }

    /** Logs an INFO message */
    public static void logInfo(String message) {
        if (extentTest.get() != null) {
            extentTest.get().info(message);
        }
    }

    /** Logs a WARNING */
    public static void logWarning(String message) {
        if (extentTest.get() != null) {
            extentTest.get().warning(message);
        }
    }

    /**
     * Attaches a screenshot to the report.
     * @param screenshotPath absolute path to the PNG file
     */
    public static void attachScreenshot(String screenshotPath) {
        if (extentTest.get() != null && screenshotPath != null && !screenshotPath.isEmpty()) {
            try {
                extentTest.get().addScreenCaptureFromPath(screenshotPath, "Failure Screenshot");
            } catch (Exception e) {
                logger.warn("Could not attach screenshot to Extent Report: " + e.getMessage());
            }
        }
    }

    /**
     * Writes the report HTML file to disk.
     * MUST be called once at the end of the run (see CucumberListener).
     */
    public static synchronized void flushReports() {
        if (extentReports != null) {
            extentReports.flush();
            logger.info("Extent Reports flushed to disk");
        }
    }
}
