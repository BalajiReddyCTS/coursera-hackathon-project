package com.hackathonProject.pages;

import com.hackathonProject.base.BaseClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import com.hackathonProject.utils.ExcelDataWriter;
import com.hackathonProject.utils.JavaScriptUtil;
import com.hackathonProject.utils.WaitUtil;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchResultsPage {

    private static final Logger logger = LogManager.getLogger(SearchResultsPage.class);

    private static final Pattern RATING_PATTERN = Pattern.compile("(\\d\\.\\d)\\s*·\\s*[\\d.,]+[Kk]?\\s*reviews?");
    private static final Pattern RATING_FALLBACK = Pattern.compile("\\b([1-5]\\.\\d)\\b");
    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+\\s*-\\s*\\d+\\s*(?:Weeks?|Months?|Hours?))");
    private static final Set<String> SKIP_NAMES = Set.of("IBM", "Meta", "Google", "Microsoft", "Coursera");

    private static final By COURSE_CARDS = By.xpath(
        "//li[.//a[contains(@href,'/learn/') or contains(@href,'/specializations/')]]" +
        "[not(ancestor::footer) and not(ancestor::nav)]");

    private WebDriver driver;

    @FindBy(xpath = "//button[@aria-label='Close']")
    private WebElement closePopupBtn;

    public SearchResultsPage() {
        this.driver = BaseClass.getDriver();
        PageFactory.initElements(driver, this);
    }

    public void selectLanguageFilter(String language) {
        logger.info("Applying language filter: " + language);
        String url = driver.getCurrentUrl();
        if (!url.contains("language=" + language)) {
            driver.navigate().to(url + (url.contains("?") ? "&" : "?") + "language=" + language);
            WaitUtil.waitForPageLoad(driver);
            dismissPopup();
        }
    }

    public void selectLevelFilter(String level) {
        logger.info("Applying level filter: " + level);
        String url = driver.getCurrentUrl();
        if (!url.contains("productDifficultyLevel=" + level)) {
            driver.navigate().to(url + (url.contains("?") ? "&" : "?") + "productDifficultyLevel=" + level);
            WaitUtil.waitForPageLoad(driver);
            dismissPopup();
        }
    }

    public List<CourseInfo> extractCourses(int count) {
        logger.info("Extracting top " + count + " courses");
        List<CourseInfo> courses = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        dismissPopup();
        WaitUtil.waitForElements(driver, COURSE_CARDS, 2, 10);

        // One scroll to trigger lazy loading
        JavaScriptUtil.scrollByPixels(driver, 800);
        JavaScriptUtil.scrollToTop(driver);

        List<WebElement> cards = driver.findElements(COURSE_CARDS);
        logger.info("Found " + cards.size() + " course cards");

        for (WebElement card : cards) {
            if (courses.size() >= count) break;

            CourseInfo info = extractFromCard(card, courses.size() + 1);
            if (info != null && !info.name.isEmpty() && seen.add(info.name.toLowerCase())) {
                courses.add(info);
                logger.info("Extracted: " + info);
            }
        }

        logger.info("Total extracted: " + courses.size());
        return courses;
    }

    private CourseInfo extractFromCard(WebElement card, int index) {
        CourseInfo info = new CourseInfo();
        info.serialNo = index;
        String cardText = card.getText();

        // Course name — from h3, skip provider names
        for (String line : cardText.split("\\n")) {
            String t = line.trim();
            if (t.length() < 10) continue;
            if (t.startsWith("Skills you") || t.startsWith("Best for:") || t.startsWith("Free Trial")) continue;
            if (t.matches(".*\\d\\.\\d.*reviews.*")) continue;
            if (t.matches("^(Beginner|Intermediate|Advanced|Mixed)\\s*·.*")) continue;
            if (t.contains("University of") || t.contains("Institute")) continue;
            if (SKIP_NAMES.contains(t)) continue;
            info.name = t;
            break;
        }

        // Rating
        Matcher rm = RATING_PATTERN.matcher(cardText);
        if (rm.find()) {
            info.rating = rm.group(1);
        } else {
            Matcher rm2 = RATING_FALLBACK.matcher(cardText);
            if (rm2.find()) info.rating = rm2.group(1);
        }

        // Duration
        Matcher dm = DURATION_PATTERN.matcher(cardText);
        if (dm.find()) info.hours = dm.group(1);

        return info;
    }

    public void saveCourseDataToExcel(List<CourseInfo> courses, String filePath) {
        logger.info("Saving " + courses.size() + " courses to Excel: " + filePath);
        ExcelDataWriter.writeCourseData(courses, filePath);
    }

    private void dismissPopup() {
        try {
            if (closePopupBtn.isDisplayed()) closePopupBtn.click();
        } catch (Exception ignored) {}
    }

    public static class CourseInfo {
        public int serialNo;
        public String name = "";
        public String rating = "N/A";
        public String hours = "N/A";

        @Override
        public String toString() {
            return serialNo + ". " + name + " | Rating: " + rating + " | Hours: " + hours;
        }
    }
}