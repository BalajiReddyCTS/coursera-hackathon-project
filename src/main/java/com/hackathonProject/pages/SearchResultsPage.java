package com.hackathonProject.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

import com.hackathonProject.base.BaseClass;
import com.hackathonProject.utils.ExcelDataWriter;
import com.hackathonProject.utils.JavaScriptUtil;
import com.hackathonProject.utils.WaitUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchResultsPage {

    private static final Logger logger = LogManager.getLogger(SearchResultsPage.class);
    private WebDriver driver;

    public SearchResultsPage() {
        this.driver = BaseClass.getDriver();
        PageFactory.initElements(driver, this);
    }

    public void selectLanguageFilter(String language) {
        logger.info("Applying language filter: " + language);
        String url = driver.getCurrentUrl();
        if (!url.contains("language=" + language)) {
            driver.get(url + (url.contains("?") ? "&" : "?") + "language=" + language);
            WaitUtil.waitForPageLoad(driver);
            dismissPopups();
        }
    }

    public void selectLevelFilter(String level) {
        logger.info("Applying level filter: " + level);
        String url = driver.getCurrentUrl();
        if (!url.contains("productDifficultyLevel=" + level)) {
            driver.get(url + (url.contains("?") ? "&" : "?") + "productDifficultyLevel=" + level);
            WaitUtil.waitForPageLoad(driver);
            dismissPopups();
        }
        try { driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE); } catch (Exception ignored) {}
    }

    public List<CourseInfo> extractCourses(int count) {
        logger.info("Extracting top " + count + " courses");
        List<CourseInfo> courses = new ArrayList<>();

        dismissPopups();

        // Wait for course cards to appear (any link to /learn/ or /specializations/)
        WaitUtil.waitForElements(driver,
            By.xpath("//li[.//a[contains(@href,'/learn/') or contains(@href,'/specializations/')]]"),
            2, 10);

        // One scroll to load more cards
        JavaScriptUtil.scrollByPixels(driver, 800);
        WaitUtil.briefPause(driver, 1000);
        JavaScriptUtil.scrollToTop(driver);
        WaitUtil.briefPause(driver, 500);

        List<WebElement> cards = findCourseCards();
        logger.info("Found " + cards.size() + " course cards");

        Set<String> seen = new HashSet<>();
        for (WebElement card : cards) {
            if (courses.size() >= count) break;
            try {
                CourseInfo info = extractFromCard(card, courses.size() + 1);
                if (info != null && !info.name.isEmpty() && !seen.contains(info.name.toLowerCase())) {
                    courses.add(info);
                    seen.add(info.name.toLowerCase());
                    logger.info("Extracted: " + info);
                }
            } catch (Exception e) {
                logger.warn("Card error: " + e.getMessage());
            }
        }

        logger.info("Total extracted: " + courses.size());
        return courses;
    }

    private List<WebElement> findCourseCards() {
        String[] selectors = {
            "li[data-testid='product-card-cds-ListCard']",
            "[data-e2e='SearchProductCard']",
            "div.cds-CommonCard-container"
        };
        for (String sel : selectors) {
            try {
                List<WebElement> cards = driver.findElements(By.cssSelector(sel));
                if (cards.size() >= 2) return cards;
            } catch (Exception ignored) {}
        }

        try {
            List<WebElement> cards = driver.findElements(By.xpath(
                "//li[.//a[contains(@href,'/learn/') or contains(@href,'/specializations/')]]" +
                "[not(ancestor::footer) and not(ancestor::nav)]"));
            if (!cards.isEmpty()) return cards;
        } catch (Exception ignored) {}

        return new ArrayList<>();
    }

    private CourseInfo extractFromCard(WebElement card, int index) {
        CourseInfo info = new CourseInfo();
        info.serialNo = index;

        // Course name from h3
        String[] titleSels = {"h3.cds-CommonCard-title", "h3 a", "h3",
            "[data-e2e='product-card-title']", "h2 a", "h2"};
        for (String sel : titleSels) {
            try {
                String t = card.findElement(By.cssSelector(sel)).getText().trim();
                if (t.length() > 5) { info.name = t; break; }
            } catch (Exception ignored) {}
        }

        if (info.name.isEmpty()) info.name = extractTitleFromCardText(card);

        // Rating + duration from card text
        String cardText = "";
        try { cardText = card.getText(); } catch (Exception ignored) {}

        info.rating = "N/A";
        try {
            java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(\\d\\.\\d)\\s*·\\s*[\\d.,]+[Kk]?\\s*reviews?").matcher(cardText);
            if (m.find()) { info.rating = m.group(1); }
            else {
                java.util.regex.Matcher m2 = java.util.regex.Pattern
                    .compile("\\b([1-5]\\.\\d)\\b").matcher(cardText);
                if (m2.find()) info.rating = m2.group(1);
            }
        } catch (Exception ignored) {}

        info.hours = "N/A";
        try {
            java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(\\d+\\s*-\\s*\\d+\\s*(?:Weeks?|Months?|Hours?)|Less Than \\d+\\s*Hours?)")
                .matcher(cardText);
            if (m.find()) info.hours = m.group(1);
        } catch (Exception ignored) {}

        return info;
    }

    private String extractTitleFromCardText(WebElement card) {
        try {
            String[] lines = card.getText().split("\\n");
            for (String line : lines) {
                String t = line.trim();
                if (t.isEmpty() || t.length() < 10) continue;
                if (t.startsWith("Skills you") || t.startsWith("Best for:") || t.startsWith("Free Trial")) continue;
                if (t.matches(".*\\d\\.\\d.*reviews.*")) continue;
                if (t.matches("^(Beginner|Intermediate|Advanced|Mixed)\\s*·.*")) continue;
                if (t.contains("University of") || t.contains("Institute")) continue;
                if (t.matches("^[A-Z][a-z]*$") && t.length() < 15) continue;
                if (t.equals("IBM") || t.equals("Meta") || t.equals("Google") || t.equals("Microsoft")) continue;
                return t;
            }
        } catch (Exception ignored) {}
        return "";
    }

    public void saveCourseDataToExcel(List<CourseInfo> courses, String filePath) {
        logger.info("Saving " + courses.size() + " courses to Excel: " + filePath);
        ExcelDataWriter.writeCourseData(courses, filePath);
    }

    private void dismissPopups() {
        for (String xpath : new String[]{"//button[@aria-label='Close']", "//button[@id='onetrust-accept-btn-handler']"}) {
            try {
                List<WebElement> btns = driver.findElements(By.xpath(xpath));
                for (WebElement btn : btns) { if (btn.isDisplayed()) btn.click(); }
            } catch (Exception ignored) {}
        }
    }

    public static class CourseInfo {
        public int serialNo;
        public String name = "";
        public String rating = "";
        public String hours = "";
        @Override
        public String toString() { return serialNo + ". " + name + " | Rating: " + rating + " | Hours: " + hours; }
    }
}