package pages;

import base.BaseClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import utils.ExcelDataWriter;
import utils.JavaScriptUtil;
import utils.WaitUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * SearchResultsPage v8
 *
 * From the actual page screenshot, each course card has:
 *   - Provider logo + "University of California, Davis" (a link — NOT the course title)
 *   - "Web Development for Beginners" (the actual title, bold text)
 *   - "Skills you'll gain: ..."
 *   - "★ 4.7 · 3.7K reviews"
 *   - "Beginner · Specialization · 3 - 6 Months"
 *
 * FIX: Extract titles from the CARD containers, not from raw links.
 * The title is the bold/heading text BELOW the provider name.
 * We find each card, then extract its title, rating, and duration separately.
 */
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
            WaitUtil.hardWait(2);
            dismissPopups();
        }
    }

    public void selectLevelFilter(String level) {
        logger.info("Applying level filter: " + level);
        String url = driver.getCurrentUrl();
        if (!url.contains("productDifficultyLevel=" + level)) {
            driver.get(url + (url.contains("?") ? "&" : "?") + "productDifficultyLevel=" + level);
            WaitUtil.waitForPageLoad(driver);
            WaitUtil.hardWait(2);
            dismissPopups();
        }
        try { driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE); } catch (Exception ignored) {}
    }

    /**
     * Extracts courses by finding CARD containers first, then pulling
     * title, rating, and duration from each card.
     */
    public List<CourseInfo> extractCourses(int count) {
        logger.info("Extracting top " + count + " courses");
        List<CourseInfo> courses = new ArrayList<>();

        WaitUtil.hardWait(2);
        dismissPopups();

        // One scroll to trigger lazy loading
        JavaScriptUtil.scrollByPixels(driver, 800);
        WaitUtil.hardWait(1);
        JavaScriptUtil.scrollToTop(driver);
        WaitUtil.hardWait(1);

        // Find all course card containers
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
                logger.warn("Card extraction error: " + e.getMessage());
            }
        }

        logger.info("Total extracted: " + courses.size());
        return courses;
    }

    /**
     * Finds course card containers. Each card is typically an <li> or <div>
     * that contains the course image, provider, title, skills, rating, and metadata.
     */
    private List<WebElement> findCourseCards() {
        // Strategy 1: Cards with product-card test IDs
        String[] cardSelectors = {
            "li[data-testid='product-card-cds-ListCard']",
            "[data-e2e='SearchProductCard']",
            "div.cds-CommonCard-container"
        };
        for (String sel : cardSelectors) {
            try {
                List<WebElement> cards = driver.findElements(By.cssSelector(sel));
                if (cards.size() >= 2) return cards;
            } catch (Exception ignored) {}
        }

        // Strategy 2: li elements that contain BOTH a course link AND rating text
        try {
            List<WebElement> cards = driver.findElements(By.xpath(
                "//li[.//a[contains(@href,'/learn/') or contains(@href,'/specializations/')] " +
                "and .//span[contains(text(),'reviews') or contains(text(),'Rating')]]"));
            if (cards.size() >= 2) return cards;
        } catch (Exception ignored) {}

        // Strategy 3: Any li with an h3 or bold course title
        try {
            List<WebElement> cards = driver.findElements(By.xpath(
                "//li[.//a[contains(@href,'/learn/') or contains(@href,'/specializations/')]]" +
                "[not(ancestor::footer) and not(ancestor::nav)]"));
            if (!cards.isEmpty()) return cards;
        } catch (Exception ignored) {}

        return new ArrayList<>();
    }

    /**
     * Extracts course info from a single card container.
     *
     * Card structure (from screenshot):
     *   [Image]
     *   Provider: "University of California, Davis"   ← skip this
     *   Title: "Web Development for Beginners"        ← we want this
     *   Skills: "Skills you'll gain: ..."
     *   Rating: "★ 4.7 · 3.7K reviews"
     *   Meta: "Beginner · Specialization · 3 - 6 Months"
     */
    private CourseInfo extractFromCard(WebElement card, int index) {
        CourseInfo info = new CourseInfo();
        info.serialNo = index;

        // === COURSE NAME ===
        // The title is the SECOND prominent text in the card (after provider name)
        // It's usually in a heading tag or a bold/styled element
        // Try specific title selectors first
        String[] titleSelectors = {
            "h3.cds-CommonCard-title",
            "h3 a",
            "h3",
            "[data-e2e='product-card-title']",
            "h2 a",
            "h2"
        };
        for (String sel : titleSelectors) {
            try {
                WebElement titleEl = card.findElement(By.cssSelector(sel));
                String title = titleEl.getText().trim();
                if (!title.isEmpty() && title.length() > 5) {
                    info.name = title;
                    break;
                }
            } catch (Exception ignored) {}
        }

        // If CSS selectors didn't work, get the card's full text and parse it
        if (info.name.isEmpty()) {
            info.name = extractTitleFromCardText(card);
        }

        // === RATING ===
        info.rating = "N/A";
        String cardText = "";
        try { cardText = card.getText(); } catch (Exception ignored) {}

        try {
            // Rating appears as "4.7 · 3.7K reviews" or just "4.7" in card text
            // The star icon is an SVG, not text — so we match the number directly
            java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(\\d\\.\\d)\\s*·\\s*[\\d.,]+[Kk]?\\s*reviews?").matcher(cardText);
            if (m.find()) {
                info.rating = m.group(1);
            } else {
                // Fallback: match any "X.X" that looks like a rating (between 1.0 and 5.0)
                java.util.regex.Matcher m2 = java.util.regex.Pattern
                    .compile("\\b([1-5]\\.\\d)\\b").matcher(cardText);
                if (m2.find()) info.rating = m2.group(1);
            }
        } catch (Exception ignored) {}

        // === DURATION ===
        info.hours = "N/A";
        try {
            // Look for pattern: "3 - 6 Months" or "1 - 4 Weeks"
            java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(\\d+\\s*-\\s*\\d+\\s*(?:Weeks?|Months?|Hours?)|Less Than \\d+\\s*Hours?)")
                .matcher(cardText);
            if (m.find()) info.hours = m.group(1);
        } catch (Exception ignored) {}

        return info;
    }

    /**
     * Extracts the course title from a card's full text.
     * The card text typically looks like:
     *   "Free Trial\nUniversity of California, Davis\nWeb Development for Beginners\nSkills you'll gain:..."
     *
     * We skip lines that are: short labels ("Free Trial"), provider names (contain "University" or are
     * known providers), "Skills you'll gain", rating lines, etc.
     * The first remaining line with > 10 chars is likely the course title.
     */
    private String extractTitleFromCardText(WebElement card) {
        try {
            String fullText = card.getText();
            String[] lines = fullText.split("\\n");

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;
                if (trimmed.length() < 10) continue;

                // Skip known non-title patterns
                if (trimmed.startsWith("Skills you")) continue;
                if (trimmed.startsWith("Best for:")) continue;
                if (trimmed.startsWith("Free Trial")) continue;
                if (trimmed.matches(".*\\d\\.\\d.*reviews.*")) continue;  // Rating line
                if (trimmed.matches("^(Beginner|Intermediate|Advanced|Mixed)\\s*·.*")) continue;  // Meta line
                if (trimmed.contains("University of") || trimmed.contains("Institute")) continue;

                // Skip short provider names (IBM, Meta, Google, Coursera, etc.)
                if (trimmed.matches("^[A-Z][a-z]*$") && trimmed.length() < 15) continue;
                if (trimmed.equals("IBM") || trimmed.equals("Meta") || trimmed.equals("Google") ||
                    trimmed.equals("Coursera") || trimmed.equals("Microsoft")) continue;

                // This line is likely the course title
                return trimmed;
            }
        } catch (Exception ignored) {}
        return "";
    }

    public void saveCourseDataToExcel(List<CourseInfo> courses, String filePath) {
        logger.info("Saving " + courses.size() + " courses to Excel: " + filePath);
        ExcelDataWriter.writeCourseData(courses, filePath);
    }

    private void dismissPopups() {
        String[] xpaths = {"//button[@aria-label='Close']", "//button[@id='onetrust-accept-btn-handler']"};
        for (String xpath : xpaths) {
            try {
                List<WebElement> btns = driver.findElements(By.xpath(xpath));
                for (WebElement btn : btns) {
                    if (btn.isDisplayed()) { btn.click(); WaitUtil.hardWait(1); return; }
                }
            } catch (Exception ignored) {}
        }
    }

    public static class CourseInfo {
        public int serialNo;
        public String name = "";
        public String rating = "";
        public String hours = "";

        @Override
        public String toString() {
            return serialNo + ". " + name + " | Rating: " + rating + " | Hours: " + hours;
        }
    }
}