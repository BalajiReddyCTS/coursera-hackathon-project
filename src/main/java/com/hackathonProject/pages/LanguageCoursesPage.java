package com.hackathonProject.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

import com.hackathonProject.base.BaseClass;
import com.hackathonProject.utils.JavaScriptUtil;
import com.hackathonProject.utils.WaitUtil;

import java.util.*;

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
        driver.get("https://www.coursera.org/courses?query=language+learning");
        WaitUtil.waitForPageLoad(driver);
        dismissPopups();
    }

    public Map<String, Integer> extractAllLanguages() {
        logger.info("Extracting all language filter options");
        Map<String, Integer> languageMap = new LinkedHashMap<>();

        try {
            openFilterPanel();
            if (!clickSectionButton("Language")) return languageMap;
            WaitUtil.briefPause(driver, 1500);
            clickShowMore();
            WaitUtil.briefPause(driver, 1000);

            for (String[] entry : extractVisibleFilterEntries()) {
                if (!isLevelEntry(entry[0])) languageMap.put(entry[0], Integer.parseInt(entry[1]));
            }
            logger.info("Total languages: " + languageMap.size());
        } catch (Exception e) {
            logger.error("Failed to extract languages", e);
        }
        return languageMap;
    }

    public Map<String, Integer> extractAllLevels() {
        logger.info("Extracting all level filter options");
        Map<String, Integer> levelMap = new LinkedHashMap<>();

        try {
            // Reload for clean state
            driver.get("https://www.coursera.org/courses?query=language+learning");
            WaitUtil.waitForPageLoad(driver);
            dismissPopups();

            openFilterPanel();
            if (!clickSectionButton("Level")) {
                return extractLevelsFallback();
            }
            WaitUtil.briefPause(driver, 1500);

            for (String[] entry : extractVisibleFilterEntries()) {
                if (isLevelEntry(entry[0])) {
                    levelMap.put(entry[0], Integer.parseInt(entry[1]));
                    logger.info("Level: " + entry[0] + " → " + entry[1]);
                }
            }

            if (levelMap.isEmpty()) levelMap = extractLevelsFallback();
        } catch (Exception e) {
            logger.error("Failed to extract levels", e);
        }
        logger.info("Total levels: " + levelMap.size());
        return levelMap;
    }

    private void openFilterPanel() {
        // Check if already open
        try {
            List<WebElement> sections = driver.findElements(
                By.xpath("//button[normalize-space(.)='Language'] | //button[normalize-space(.)='Level']"));
            for (WebElement s : sections) { if (s.isDisplayed()) return; }
        } catch (Exception ignored) {}

        WebElement filterBtn = WaitUtil.waitForAnyElement(driver, 8,
            By.xpath("//button[contains(.,'Filter') and contains(.,'Sort')]"),
            By.xpath("//button[contains(.,'Filters')]"));
        if (filterBtn != null) {
            JavaScriptUtil.scrollAndClick(driver, filterBtn);
            // Wait for the panel sections to appear
            WaitUtil.waitForAnyElement(driver, 5,
                By.xpath("//button[normalize-space(.)='Language']"),
                By.xpath("//button[normalize-space(.)='Level']"));
            logger.info("Opened Filter panel");
        }
    }

    private boolean clickSectionButton(String name) {
        String[] xpaths = {
            "//button[normalize-space(.)='" + name + "']",
            "//button[contains(.,'" + name + "') and not(contains(.,'Filter'))]",
            "//span[text()='" + name + "']/ancestor::button"
        };
        for (String xpath : xpaths) {
            try {
                List<WebElement> btns = driver.findElements(By.xpath(xpath));
                for (WebElement btn : btns) {
                    if (btn.isDisplayed() && !btn.getText().contains("Filter & Sort")) {
                        JavaScriptUtil.scrollAndClick(driver, btn);
                        logger.info("Clicked '" + name + "' section");
                        // Wait for labels to appear
                        WaitUtil.waitForElement(driver, By.xpath("//label[contains(text(),'(')]"), 5);
                        return true;
                    }
                }
            } catch (Exception ignored) {}
        }
        logger.warn("'" + name + "' section not found");
        return false;
    }

    private void clickShowMore() {
        try {
            List<WebElement> btns = driver.findElements(By.xpath("//button[contains(.,'Show more')]"));
            for (WebElement btn : btns) {
                if (btn.isDisplayed()) {
                    JavaScriptUtil.scrollAndClick(driver, btn);
                    WaitUtil.briefPause(driver, 1000);
                    return;
                }
            }
        } catch (Exception ignored) {}
    }

    private List<String[]> extractVisibleFilterEntries() {
        List<String[]> results = new ArrayList<>();
        List<WebElement> labels = new ArrayList<>();

        for (String xpath : new String[]{"//label[.//input[@type='checkbox']]", "//label[contains(text(),'(')]"}) {
            try {
                for (WebElement el : driver.findElements(By.xpath(xpath))) {
                    if (el.isDisplayed()) {
                        String text = getElementText(el);
                        if (!text.isEmpty() && text.contains("(")) labels.add(el);
                    }
                }
                if (labels.size() >= 2) break;
            } catch (Exception ignored) {}
        }

        Set<String> seen = new HashSet<>();
        for (WebElement label : labels) {
            String[] parsed = parseNameAndCount(getElementText(label));
            if (parsed[0] != null && !parsed[0].isEmpty() && seen.add(parsed[0])) {
                results.add(parsed);
            }
        }
        return results;
    }

    private boolean isLevelEntry(String name) {
        if (name == null) return false;
        for (String level : KNOWN_LEVELS) {
            if (name.trim().equalsIgnoreCase(level) || name.trim().toLowerCase().startsWith(level.toLowerCase()))
                return true;
        }
        return false;
    }

    private Map<String, Integer> extractLevelsFallback() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (String level : new String[]{"Beginner", "Intermediate", "Advanced", "Mixed"}) {
            try {
                for (WebElement el : driver.findElements(
                    By.xpath("//*[contains(text(),'" + level + "') and contains(text(),'(')]"))) {
                    if (el.isDisplayed()) {
                        String[] parsed = parseNameAndCount(getElementText(el));
                        if (parsed[0] != null && isLevelEntry(parsed[0])) {
                            map.put(parsed[0], Integer.parseInt(parsed[1]));
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
        return map;
    }

    private String getElementText(WebElement el) {
        try { String t = el.getText().trim(); if (!t.isEmpty()) return t; } catch (Exception ignored) {}
        try {
            return ((String) ((JavascriptExecutor) driver).executeScript("return arguments[0].innerText;", el)).trim();
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
        for (String xpath : new String[]{"//button[@aria-label='Close']", "//button[@id='onetrust-accept-btn-handler']"}) {
            try {
                for (WebElement btn : driver.findElements(By.xpath(xpath))) { if (btn.isDisplayed()) btn.click(); }
            } catch (Exception ignored) {}
        }
    }
}