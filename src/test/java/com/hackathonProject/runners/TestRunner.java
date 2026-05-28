package com.hackathonProject.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;


@CucumberOptions(

    features = "src/test/resources/features",

    glue = {
        "com.hackathonProject.stepdefinitions",  
        "com.hackathonProject.hooks"            
    },

    plugin = {
        
        "pretty",

        // HTML report
        "html:reports/cucumber/cucumber-report.html",

        // JSON report 
        "json:reports/cucumber/cucumber-report.json",

        // Allure report data 8
        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",

        // ExtentReports Cucumber adapter
        "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:",

        // OUR CUSTOM LISTENER — listens to events and logs them
        "com.hackathonProject.listeners.CucumberListener"
    },

        // OR — run scenarios that have @Smoke OR @Regression
        tags = "@Smoke or @Regression",

        // AND — run scenarios that have BOTH @Smoke AND @Regression
        //tags = "@Smoke and @Regression"

        // NOT — run @Smoke but exclude @Regression
        //tags = "@Smoke and not @Regression"
    
    monochrome = true,

    publish = false

)

public class TestRunner extends AbstractTestNGCucumberTests {

//    AbstractTestNGCucumberTests (Cucumber's built-in TestNG bridge) implements scenarios() —
//    it scans all  .feature files and returns each scenario as one row in a 2D array

    @Override
    @DataProvider(parallel = true) //TestNG feeds all rows simultaneously to separate threads
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
