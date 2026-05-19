package com.hackathonProject.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.hackathonProject.base.BaseClass;
import com.hackathonProject.utils.WaitUtil;

public class HomePage {

	private static final Logger logger = LogManager.getLogger(HomePage.class);
    private WebDriver driver;
 
    @FindBy(name = "query")
    private WebElement searchBox;
 
    @FindBy(xpath = "//button[@aria-label='Close']")
    private WebElement closePopupBtn;
 
    public HomePage() {
        this.driver = BaseClass.getDriver();
        PageFactory.initElements(driver, this);
    }
 
    public void openHomePage(String url) {
        logger.info("Navigating to: " + url);
        driver.get(url);
        WaitUtil.waitForPageLoad(driver);
        dismissPopup();
    }
 
    public void searchFor(String searchTerm) {
        logger.info("Searching for: " + searchTerm);
        dismissPopup();
        WaitUtil.waitForElementVisible(driver, searchBox);
        searchBox.click();
        searchBox.clear();
        searchBox.sendKeys(searchTerm);
        searchBox.sendKeys(Keys.ENTER);
        WaitUtil.waitForPageLoad(driver);
        dismissPopup();
        logger.info("Search submitted. URL: " + driver.getCurrentUrl());
    }
 
    public String getPageTitle() {
        return driver.getTitle();
    }
 
    private void dismissPopup() {
        try {
            if (closePopupBtn.isDisplayed()) closePopupBtn.click();
        } catch (Exception ignored) {}
    }
}