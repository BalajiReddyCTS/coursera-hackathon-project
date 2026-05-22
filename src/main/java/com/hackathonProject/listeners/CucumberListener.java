package com.hackathonProject.listeners;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class CucumberListener implements ConcurrentEventListener {

    private static final Logger logger = LogManager.getLogger(CucumberListener.class);

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestCaseStarted.class, this::onScenarioStart);
        publisher.registerHandlerFor(TestStepFinished.class, this::onStepFinished);
        publisher.registerHandlerFor(TestCaseFinished.class, this::onScenarioFinished);
        publisher.registerHandlerFor(TestRunFinished.class, this::onRunFinished);
    }

    private void onScenarioStart(TestCaseStarted event) {
        String scenarioName = event.getTestCase().getName();
        String uri = event.getTestCase().getUri().toString();
        logger.info("▶ SCENARIO STARTED: [" + scenarioName + "] in [" + uri + "]");
    }

    private void onStepFinished(TestStepFinished event) {
        if (event.getTestStep() instanceof PickleStepTestStep) {
            PickleStepTestStep step = (PickleStepTestStep) event.getTestStep();
            String stepText = step.getStep().getText();
            Result result = event.getResult();

            switch (result.getStatus()) {
                case PASSED:
                    logger.info("   ✓ STEP PASSED: " + stepText);
                    break;
                case FAILED:
                    logger.error("   ✗ STEP FAILED: " + stepText);
                    if (result.getError() != null) {
                        logger.error("     Error: " + result.getError().getMessage());
                    }
                    break;
                case SKIPPED:
                    logger.warn("   ⊘ STEP SKIPPED: " + stepText);
                    break;
                case PENDING:
                    logger.warn("   ? STEP PENDING: " + stepText);
                    break;
                default:
                    logger.info("   ~ STEP [" + result.getStatus() + "]: " + stepText);
            }
        }
    }

    private void onScenarioFinished(TestCaseFinished event) {
        String scenarioName = event.getTestCase().getName();
        Status status = event.getResult().getStatus();
        double durationSeconds = event.getResult().getDuration().toNanos() / 1_000_000_000.0;

        if (status == Status.PASSED) {
            logger.info(String.format("■ SCENARIO PASSED: [%s] in %.2fs", scenarioName, durationSeconds));
        } else {
            logger.error(String.format("■ SCENARIO FAILED: [%s] in %.2fs | Status: %s",
                scenarioName, durationSeconds, status));
        }
    }

    private void onRunFinished(TestRunFinished event) {
        logger.info("========================================");
        logger.info("  CUCUMBER TEST RUN FINISHED");
        logger.info("========================================");

        // Flush Extent Reports
        try {
            com.hackathonProject.utils.ExtentReportManager.flushReports();
            logger.info("Extent Reports flushed successfully");
        } catch (Exception e) {
            logger.error("Could not flush Extent Reports: " + e.getMessage());
        }

        // Archive Cucumber reports with timestamp
        archiveCucumberReports();
    }

    private void archiveCucumberReports() {
        String timestamp = com.hackathonProject.utils.ExtentReportManager.getTimestamp();

        copyFile("reports/cucumber/cucumber-report.html",
                 "reports/cucumber/cucumber-report_" + timestamp + ".html");

        copyFile("reports/cucumber/cucumber-report.json",
                 "reports/cucumber/cucumber-report_" + timestamp + ".json");
    }

    private void copyFile(String source, String destination) {
        try {
            File src = new File(source);
            if (src.exists()) {
                Files.copy(src.toPath(), Path.of(destination), StandardCopyOption.REPLACE_EXISTING);
                logger.info("Report archived: " + destination);
            }
        } catch (Exception e) {
            logger.warn("Could not archive report: " + e.getMessage());
        }
    }
}