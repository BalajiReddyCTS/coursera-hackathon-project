package com.hackathonProject.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * TestRunner - the entry point for running all Cucumber tests via TestNG.
 *
 * HOW IT WORKS:
 * - Extends AbstractTestNGCucumberTests (Cucumber's TestNG bridge)
 * - @CucumberOptions configures which features, glue code, and plugins to use
 * - @DataProvider(parallel=true) enables parallel scenario execution
 *
 * TO RUN:
 *   mvn test                          → runs all features
 *   mvn test -Dcucumber.filter.tags=@Smoke   → runs only @Smoke scenarios
 *   mvn test -Dbrowser=firefox        → runs in Firefox
 */
@CucumberOptions(

    // ---- Features: where .feature files live ----
    features = "src/test/resources/features",

    // ---- Glue: packages containing step definitions AND hooks ----
    glue = {
        "com.hackathonProject.stepdefinitions",  // Step def classes
        "com.hackathonProject.hooks"             // CucumberHooks (@Before/@After)
    },

    // ---- Tags: run specific scenarios (can be overridden from command line) ----
    // tags = "@Smoke",   // Uncomment to run only @Smoke scenarios

    // ---- Plugins: report generators and the custom listener ----
    plugin = {
        // Cucumber's built-in pretty console output
        "pretty",

        // Cucumber's own HTML report
        "html:reports/cucumber/cucumber-report.html",

        // Cucumber JSON (needed by some CI tools and Extent adapters)
        "json:reports/cucumber/cucumber-report.json",

        // Allure report data (allure-maven plugin converts this to HTML)
        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",

        // ExtentReports Cucumber adapter
        "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:",

        // OUR CUSTOM LISTENER — listens to events and logs them
        "com.hackathonProject.listeners.CucumberListener"
    },

    // ---- Monochrome: clean console output (no ANSI colour codes) ----
    monochrome = true,

    // ---- Publish: Cucumber's cloud publish (optional) ----
    publish = false

)
public class TestRunner extends AbstractTestNGCucumberTests {

    /**
     * PARALLEL EXECUTION:
     * Overriding dataProvider() with parallel=true tells TestNG to run
     * each Cucumber scenario in its own thread simultaneously.
     *
     * The number of threads is controlled in testng.xml (thread-count).
     * ThreadLocal in BaseClass ensures each thread has its own WebDriver.
     */
    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
