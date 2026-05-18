# CourseraAutomation — BDD Selenium Framework

A beginner-friendly BDD automation project using Cucumber + Selenium + TestNG.

---

## Project Overview

### Three End-to-End Flows

| Flow | Description |
|------|-------------|
| **Flow 1** | Search for "web development courses for beginners" → filter English + Beginner → extract first **10** courses (name, hours, rating) → save to **Excel** |
| **Flow 2** | Navigate to Language Learning → open Language filter → extract **all languages with counts** → extract **all levels with counts** |
| **Flow 3** | Go to Coursera For Business → fill "Ready to learn more?" contact form → enter **invalid email** → capture and assert the error message |

---

## Tech Stack

| Tool | Purpose |
|------|---------|
| Java 11 | Programming language |
| Maven | Build & dependency management |
| Selenium 4 | Browser automation |
| WebDriverManager | Auto-downloads chromedriver/geckodriver |
| Cucumber 7 (BDD) | Given/When/Then test scenarios |
| TestNG | Test runner, parallel execution, assertions |
| Apache POI | Read/write Excel files |
| Extent Reports | Custom HTML test report |
| Allure | Rich visual HTML report |
| Log4j2 | Logging to console and file |

---

## Prerequisites

- Java 11+ installed (`java -version`)
- Maven 3.6+ installed (`mvn -version`)
- Chrome browser installed
- Internet connection (tests run against live Coursera site)

---

## Project Structure

```
CourseraAutomation/
├── pom.xml                          ← Maven dependencies
├── testng.xml                       ← TestNG suite (parallel config)
├── allure.properties                ← Allure output path
│
├── src/
│   ├── main/java/
│   │   ├── base/
│   │   │   └── BaseClass.java       ← ThreadLocal<WebDriver> — parallel-safe
│   │   ├── pages/                   ← Page Object Model (POM)
│   │   │   ├── HomePage.java
│   │   │   ├── SearchResultsPage.java
│   │   │   ├── LanguageCoursesPage.java
│   │   │   └── CampusPage.java
│   │   ├── hooks/
│   │   │   └── CucumberHooks.java   ← @Before → launch, @After → quit + screenshot
│   │   ├── listeners/
│   │   │   └── CucumberListener.java ← ONE file, ConcurrentEventListener
│   │   ├── utils/
│   │   │   ├── ConfigReader.java    ← Reads config.properties
│   │   │   ├── ExcelDataWriter.java ← Apache POI — writes course data to .xlsx
│   │   │   ├── WaitUtil.java        ← Smart waits (explicit/implicit)
│   │   │   ├── JavaScriptUtil.java  ← JS scrolling and clicking
│   │   │   ├── ScreenshotUtil.java  ← Captures screenshots on failure
│   │   │   └── ExtentReportManager.java ← Extent HTML report
│   │   └── constants/
│   │       └── FrameworkConstants.java ← URLs, paths, test data
│   │
│   └── main/resources/
│       ├── config/config.properties ← browser, URL, timeouts
│       └── log4j/log4j2.xml         ← Logging configuration
│
├── src/test/
│   ├── java/
│   │   ├── stepdefinitions/
│   │   │   ├── CourseSearchSteps.java
│   │   │   ├── LanguageLearningSteps.java
│   │   │   └── EnterpriseFormSteps.java
│   │   └── runners/
│   │       └── TestRunner.java      ← @CucumberOptions + parallel=true
│   │
│   └── resources/
│       ├── features/
│       │   ├── CourseSearch.feature
│       │   ├── LanguageLearning.feature
│       │   └── EnterpriseForm.feature
│       ├── testdata/                ← Excel output saved here
│       └── extent.properties        ← Extent adapter config
│
├── screenshots/                     ← Auto-saved on failure
├── reports/
│   ├── extent/ExtentReport.html     ← Extent HTML report
│   └── cucumber/                    ← Cucumber HTML + JSON
└── logs/automation.log              ← Log4j2 log file
```

---

## How to Run

### Run all tests (parallel, 3 threads)
```bash
cd CourseraAutomation
mvn clean test
```

### Run only one flow by tag
```bash
mvn test -Dcucumber.filter.tags="@CourseSearch"
mvn test -Dcucumber.filter.tags="@LanguageLearning"
mvn test -Dcucumber.filter.tags="@EnterpriseForm"
```

### Run in a different browser
```bash
mvn test -Dbrowser=firefox
mvn test -Dbrowser=edge
```

### Generate Allure report (after test run)
```bash
mvn allure:report
# Then open: target/site/allure-maven-plugin/index.html
```

---

## Reports

| Report | Location |
|--------|----------|
| Extent HTML | `reports/extent/ExtentReport.html` |
| Cucumber HTML | `reports/cucumber/cucumber-report.html` |
| Allure HTML | `target/site/allure-maven-plugin/index.html` |
| Log file | `logs/automation.log` |
| Excel output | `src/test/resources/testdata/CourseData.xlsx` |
| Screenshots | `screenshots/` (on failure) |

---

## Key Concepts Implemented

| Concept | Where |
|---------|-------|
| BDD (Given/When/Then) | All `.feature` files |
| POM Design Pattern | `pages/` package |
| ThreadLocal WebDriver | `BaseClass.java` |
| Parallel Execution | `testng.xml` + `@DataProvider(parallel=true)` |
| Apache POI (Excel) | `ExcelDataWriter.java` |
| Cucumber Listener | `CucumberListener.java` (ConcurrentEventListener) |
| Log4j2 Logging | All classes + `log4j2.xml` |
| Screenshot on Failure | `CucumberHooks.java` + `ScreenshotUtil.java` |
| Extent Reports | `ExtentReportManager.java` |
| Allure Reports | `allure-cucumber7-jvm` plugin in pom.xml |
| Multiple Locators | XPath, CSS, Name, ID, LinkText, PartialLinkText |
| Exception Handling | Try-catch in all page methods |
| Assertions | TestNG Assert in step definitions |
| DataTable (Cucumber) | `EnterpriseFormSteps.java` — form data |
| Config-Driven | `config.properties` + `ConfigReader.java` |
| Data-Driven (Excel) | `ExcelDataWriter.readTestData()` |

---

## Notes for Beginners

- **Never modify page elements in step definitions** — always go through Page Objects.
- **ThreadLocal is crucial for parallel runs** — without it, threads share one browser and crash.
- **`@Before` and `@After` hooks** are in `CucumberHooks`, not in TestRunner.
- **Extent Reports must be flushed** — this happens in `CucumberListener.onRunFinished()`.
- **Log4j config** (`log4j2.xml`) must be on the classpath — it's in `src/main/resources/log4j/`.
