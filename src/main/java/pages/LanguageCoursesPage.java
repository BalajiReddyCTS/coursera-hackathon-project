package pages;

import base.BaseClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import utils.JavaScriptUtil;
import utils.WaitUtil;

import java.util.*;

/**
 * LanguageCoursesPage - handles Language Learning section of Coursera.
 *
 * The /courses?query=language+learning page has:
 * - "Filter & Sort" button that opens a sidebar
 * - Inside the sidebar: Language section, Level section (collapsible)
 * - Each section has labels with checkboxes: "English (9690)"
 * - Language section has a "Show more" to expand the full list
 *
 * KEY FIX v6:
 * - After extracting languages, we RELOAD the page to get a clean state
 * - Then open the filter panel AGAIN and click the LEVEL section
 * - Filter labels by known level names to guarantee purity
 */
public class LanguageCoursesPage {

    private static final Logger logger = LogManager.getLogger(LanguageCoursesPage.class);
    private WebDriver driver;

    private static final Set<String> KNOWN_LEVELS = new HashSet<>(Arrays.asList(
        "Beginner", "Intermediate", "Advanced", "Mixed"
    ));

    public LanguageCoursesPage() {
        this.driver = BaseClass.getDriver();
        PageFactory.initElements(driver, this);
    }

    public void navigateToLanguageLearning() {
        String url = "https://www.coursera.org/courses?query=language+learning";
        logger.info("Navigating to Language Learning: " + url);
        driver.get(url);
        WaitUtil.waitForPageLoad(driver);
        WaitUtil.hardWait(4);
        dismissPopups();
    }

    // ===== Language Extraction =====

    public Map<String, Integer> extractAllLanguages() {
        logger.info("Extracting all language filter options");
        Map<String, Integer> languageMap = new LinkedHashMap<>();

        try {
            openFilterPanel();
            WaitUtil.hardWait(1);

            boolean opened = clickSectionButton("Language");
            if (!opened) {
                logger.warn("Could not open Language section");
                return languageMap;
            }
            WaitUtil.hardWait(2);
            clickShowMore();
            WaitUtil.hardWait(1);

            List<String[]> entries = extractVisibleFilterEntries();
            for (String[] entry : entries) {
                if (!isLevelEntry(entry[0])) {
                    languageMap.put(entry[0], Integer.parseInt(entry[1]));
                }
            }
            logger.info("Total languages found: " + languageMap.size());

        } catch (Exception e) {
            logger.error("Failed to extract languages: " + e.getMessage(), e);
        }

        return languageMap;
    }

    // ===== Level Extraction =====

    public Map<String, Integer> extractAllLevels() {
        logger.info("Extracting all level filter options");
        Map<String, Integer> levelMap = new LinkedHashMap<>();

        try {
            // RELOAD page to get completely clean filter state
            driver.get("https://www.coursera.org/courses?query=language+learning");
            WaitUtil.waitForPageLoad(driver);
            WaitUtil.hardWait(4);
            dismissPopups();

            // Open the filter panel fresh
            openFilterPanel();
            WaitUtil.hardWait(1);

            // Click the Level section
            boolean opened = clickSectionButton("Level");
            if (!opened) {
                logger.warn("Could not open Level section, trying fallback");
                levelMap = extractLevelsFallback();
                return levelMap;
            }
            WaitUtil.hardWait(2);

            // Extract entries and filter to known levels only
            List<String[]> entries = extractVisibleFilterEntries();
            logger.info("Found " + entries.size() + " entries in Level section");

            for (String[] entry : entries) {
                if (isLevelEntry(entry[0])) {
                    levelMap.put(entry[0], Integer.parseInt(entry[1]));
                    logger.info("Level: " + entry[0] + " | Count: " + entry[1]);
                }
            }

            if (levelMap.isEmpty()) {
                logger.warn("No levels found from filter panel, trying fallback");
                levelMap = extractLevelsFallback();
            }

        } catch (Exception e) {
            logger.error("Failed to extract levels: " + e.getMessage(), e);
        }

        logger.info("Total levels found: " + levelMap.size());
        return levelMap;
    }

    // ===== Private Helpers =====

    /** Opens the "Filter & Sort" panel if not already open */
    private void openFilterPanel() {
        // Check if the panel is already open by looking for section buttons
        try {
            List<WebElement> sections = driver.findElements(
                By.xpath("//button[normalize-space(.)='Language'] | //button[normalize-space(.)='Level']"));
            for (WebElement s : sections) {
                if (s.isDisplayed()) {
                    logger.info("Filter panel already open");
                    return;
                }
            }
        } catch (Exception ignored) {}

        // Click the "Filter & Sort" button
        String[] xpaths = {
            "//button[contains(.,'Filter') and contains(.,'Sort')]",
            "//button[contains(.,'Filter & Sort')]",
            "//button[contains(.,'Filters')]"
        };
        for (String xpath : xpaths) {
            try {
                WebElement btn = driver.findElement(By.xpath(xpath));
                if (btn.isDisplayed()) {
                    JavaScriptUtil.scrollAndClick(driver, btn);
                    WaitUtil.hardWait(2);
                    logger.info("Opened Filter & Sort panel");
                    return;
                }
            } catch (Exception ignored) {}
        }
        logger.warn("Could not find Filter & Sort button");
    }

    /** Clicks a section button (Language or Level) inside the filter panel */
    private boolean clickSectionButton(String sectionName) {
        String[] xpaths = {
            "//button[normalize-space(.)='" + sectionName + "']",
            "//button[contains(.,'" + sectionName + "') and not(contains(.,'Filter'))]",
            "//span[text()='" + sectionName + "']/ancestor::button",
            "//button[@data-testid='filter-dropdown-" + sectionName.toLowerCase() + "']"
        };

        for (String xpath : xpaths) {
            try {
                List<WebElement> buttons = driver.findElements(By.xpath(xpath));
                for (WebElement btn : buttons) {
                    if (btn.isDisplayed()) {
                        String text = btn.getText().trim();
                        if (text.contains("Filter & Sort") || text.contains("Filter and Sort")) continue;
                        JavaScriptUtil.scrollAndClick(driver, btn);
                        logger.info("Clicked '" + sectionName + "' section button");
                        WaitUtil.hardWait(2);
                        return true;
                    }
                }
            } catch (Exception ignored) {}
        }
        logger.warn("Section button '" + sectionName + "' not found");
        return false;
    }

    private void clickShowMore() {
        try {
            List<WebElement> btns = driver.findElements(By.xpath("//button[contains(.,'Show more')]"));
            for (WebElement btn : btns) {
                if (btn.isDisplayed()) {
                    JavaScriptUtil.scrollAndClick(driver, btn);
                    logger.info("Clicked 'Show more'");
                    WaitUtil.hardWait(2);
                    return;
                }
            }
        } catch (Exception ignored) {}
    }

    /** Extracts all visible labels with "(count)" pattern */
    private List<String[]> extractVisibleFilterEntries() {
        List<String[]> results = new ArrayList<>();
        String[] xpaths = {
            "//label[.//input[@type='checkbox']]",
            "//label[contains(text(),'(')]"
        };

        List<WebElement> labels = new ArrayList<>();
        for (String xpath : xpaths) {
            try {
                List<WebElement> found = driver.findElements(By.xpath(xpath));
                for (WebElement label : found) {
                    try {
                        if (label.isDisplayed()) {
                            String text = getElementText(label);
                            if (!text.isEmpty() && text.contains("(")) labels.add(label);
                        }
                    } catch (Exception ignored) {}
                }
                if (labels.size() >= 2) break;
            } catch (Exception ignored) {}
        }

        Set<String> seen = new HashSet<>();
        for (WebElement label : labels) {
            try {
                String fullText = getElementText(label);
                if (fullText.isEmpty()) continue;
                String[] parsed = parseNameAndCount(fullText);
                if (parsed[0] != null && !parsed[0].isEmpty() && !seen.contains(parsed[0])) {
                    results.add(parsed);
                    seen.add(parsed[0]);
                }
            } catch (Exception ignored) {}
        }
        return results;
    }

    private boolean isLevelEntry(String name) {
        if (name == null) return false;
        for (String level : KNOWN_LEVELS) {
            if (name.trim().equalsIgnoreCase(level)) return true;
            if (name.trim().toLowerCase().startsWith(level.toLowerCase())) return true;
        }
        return false;
    }

    /** Fallback: search page for elements containing known level names */
    private Map<String, Integer> extractLevelsFallback() {
        Map<String, Integer> levelMap = new LinkedHashMap<>();
        for (String level : new String[]{"Beginner", "Intermediate", "Advanced", "Mixed"}) {
            try {
                List<WebElement> elements = driver.findElements(
                    By.xpath("//*[contains(text(),'" + level + "') and contains(text(),'(')]"));
                for (WebElement el : elements) {
                    if (el.isDisplayed()) {
                        String text = getElementText(el);
                        String[] parsed = parseNameAndCount(text);
                        if (parsed[0] != null && isLevelEntry(parsed[0])) {
                            levelMap.put(parsed[0], Integer.parseInt(parsed[1]));
                            logger.info("Fallback Level: " + parsed[0] + " → " + parsed[1]);
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
        return levelMap;
    }

    private String getElementText(WebElement element) {
        try { String t = element.getText().trim(); if (!t.isEmpty()) return t; } catch (Exception ignored) {}
        try {
            String t = (String) ((JavascriptExecutor) driver).executeScript("return arguments[0].innerText;", element);
            if (t != null && !t.trim().isEmpty()) return t.trim();
        } catch (Exception ignored) {}
        return "";
    }

    private String[] parseNameAndCount(String text) {
        text = text.replaceAll("[\\r\\n]+", " ").trim();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("^(.+?)\\s*\\(([\\d,]+)\\)").matcher(text);
        if (m.find()) return new String[]{m.group(1).trim(), m.group(2).replace(",", "")};
        return new String[]{null, "0"};
    }

    private void dismissPopups() {
        try {
            List<WebElement> btns = driver.findElements(By.xpath(
                "//button[@aria-label='Close'] | //button[@id='onetrust-accept-btn-handler']"));
            for (WebElement btn : btns) {
                if (btn.isDisplayed()) { btn.click(); WaitUtil.hardWait(1); return; }
            }
        } catch (Exception ignored) {}
    }
}