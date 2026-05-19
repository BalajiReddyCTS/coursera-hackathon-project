package com.hackathonProject.listeners;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CucumberListener implements ConcurrentEventListener {

    private static final Logger logger = LogManager.getLogger(CucumberListener.class);

    @Override
    public void setEventPublisher(EventPublisher publisher) {

        // ---- Scenario Started ----
        publisher.registerHandlerFor(TestCaseStarted.class, this::onScenarioStart);

        // ---- Step Finished (each Given/When/Then) ----
        publisher.registerHandlerFor(TestStepFinished.class, this::onStepFinished);

        // ---- Scenario Finished ----
        publisher.registerHandlerFor(TestCaseFinished.class, this::onScenarioFinished);

        // ---- Entire Run Finished ----
        publisher.registerHandlerFor(TestRunFinished.class, this::onRunFinished);
    }

    // ===== Event Handlers =====
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
        long durationNanos = event.getResult().getDuration().toNanos();
        double durationSeconds = durationNanos / 1_000_000_000.0;

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

        // Flush Extent Reports when run ends
        try {
            com.hackathonProject.utils.ExtentReportManager.flushReports();
            logger.info("Extent Reports flushed successfully");
        } catch (Exception e) {
            logger.error("Could not flush Extent Reports: " + e.getMessage());
        }
    }
}
