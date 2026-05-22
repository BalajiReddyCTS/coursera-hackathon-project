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

        // Allure report data 
        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",

        // ExtentReports Cucumber adapter
        "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:",

        // OUR CUSTOM LISTENER — listens to events and logs them
        "com.hackathonProject.listeners.CucumberListener"
    },

    tags="@Smoke",
    
    monochrome = true,

    publish = false

)
public class TestRunner extends AbstractTestNGCucumberTests {

//	@BeforeClass
//    @Parameters({"browser"})
//    public void setUpBrowser(String browser) {
//        BaseClass.setBrowserOverride(browser);
//    }
    
    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
