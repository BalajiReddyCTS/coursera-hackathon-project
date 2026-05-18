package pages;

import base.BaseClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import utils.WaitUtil;

import java.util.List;

public class HomePage {

    private static final Logger logger = LogManager.getLogger(HomePage.class);
    private WebDriver driver;

    @FindBy(name = "query")
    private WebElement searchBox;

    public HomePage() {
        this.driver = BaseClass.getDriver();
        PageFactory.initElements(driver, this);
    }

    public void openHomePage(String url) {
        logger.info("Navigating to: " + url);
        driver.get(url);
        WaitUtil.waitForPageLoad(driver);
        WaitUtil.hardWait(2);
        dismissAllPopups();
    }

    public void searchFor(String searchTerm) {
        logger.info("Searching for: " + searchTerm);
        dismissAllPopups();

        WebElement searchInput = findSearchBox();
        if (searchInput != null) {
            try {
                WaitUtil.waitForElementVisible(driver, searchInput);
                searchInput.click();
                WaitUtil.hardWait(1);
                searchInput.clear();
                searchInput.sendKeys(searchTerm);
                WaitUtil.hardWait(1);
                searchInput.sendKeys(Keys.ENTER);
                WaitUtil.waitForPageLoad(driver);
                WaitUtil.hardWait(3);
                dismissAllPopups();
                logger.info("Search submitted. URL: " + driver.getCurrentUrl());
                return;
            } catch (Exception e) {
                logger.warn("Search box interaction failed: " + e.getMessage());
            }
        }

        // Fallback: direct URL
        String encodedQuery = searchTerm.replace(" ", "+");
        driver.get("https://www.coursera.org/search?query=" + encodedQuery);
        WaitUtil.waitForPageLoad(driver);
        WaitUtil.hardWait(3);
        dismissAllPopups();
    }

    private WebElement findSearchBox() {
        try { if (searchBox != null && searchBox.isDisplayed()) return searchBox; } catch (Exception ignored) {}
        try { WebElement e = driver.findElement(By.name("query")); if (e.isDisplayed()) return e; }
        catch (Exception ignored) {}

        String[] css = {"input[type='search']", "input[placeholder*='Search']",
            "input[aria-label*='Search']", "input[data-testid='search-input']"};
        for (String s : css) {
            try {
                List<WebElement> els = driver.findElements(By.cssSelector(s));
                for (WebElement e : els) { if (e.isDisplayed()) return e; }
            } catch (Exception ignored) {}
        }

        // Click search icon to reveal input
        try {
            WebElement icon = driver.findElement(By.cssSelector("button[aria-label*='Search']"));
            if (icon.isDisplayed()) {
                icon.click(); WaitUtil.hardWait(1);
                try { WebElement e = driver.findElement(By.name("query")); if (e.isDisplayed()) return e; }
                catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}

        return null;
    }

    public void clickForBusiness() {
        try {
            WebElement link = driver.findElement(By.linkText("For Businesses"));
            link.click();
        } catch (Exception e) {
            try { driver.findElement(By.xpath("//a[contains(text(),'For Business')]")).click(); }
            catch (Exception e2) { driver.get("https://www.coursera.org/business"); WaitUtil.waitForPageLoad(driver); }
        }
    }

    public String getPageTitle() { return driver.getTitle(); }

    /** Dismisses ALL popups — announcement banners, cookie consent, etc. */
    private void dismissAllPopups() {
        String[] xpaths = {
            "//button[@aria-label='Close']",
            "//button[@id='onetrust-accept-btn-handler']",
            "//button[contains(@class,'close')]",
            "//button[text()='✕']",
            "//button[text()='×']"
        };
        for (String xpath : xpaths) {
            try {
                List<WebElement> btns = driver.findElements(By.xpath(xpath));
                for (WebElement btn : btns) {
                    if (btn.isDisplayed()) {
                        btn.click();
                        logger.info("Dismissed popup");
                        WaitUtil.hardWait(1);
                    }
                }
            } catch (Exception ignored) {}
        }
    }
}