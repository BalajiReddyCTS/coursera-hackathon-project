# CourseraAutomation — BDD Selenium Framework

A Selenium + Cucumber BDD + TestNG automation framework for Coursera.org, with parallel execution, cross-browser support, and triple reporting (Cucumber HTML, Extent, Allure).

---

## Project Overview

### Three End-to-End Flows

| Flow | Description |
|------|-------------|
| **Flow 1 – Course Search** | Search for "web development courses for beginners" → apply English + Beginner filters → extract the first **5** courses (name, learning hours, rating) → save to `CourseData.xlsx` via Apache POI |
| **Flow 2 – Language Learning** | Navigate to the Language Learning category → open the Language filter and capture every language with its course count → reload, open the Level filter, capture every level (Beginner / Intermediate / Advanced / Mixed) with counts |
| **Flow 3 – Enterprise Form** | Go to Coursera For Business → scroll to the Marketo "Ready to learn more?" form → fill First Name, Last Name and an **invalid email** (no `@`) → tab out → submit → assert the validation error contains "valid email" |

All three scenarios are tagged `@Smoke`, and `TestRunner` filters on that tag.

---

## Tech Stack

| Tool | Version | Purpose |
|------|---------|---------|
| Java | 11 | Language / compile target |
| Maven | 3.6+ | Build & dependency management |
| Selenium WebDriver | 4.18.1 | Browser automation |
| WebDriverManager | 5.7.0 | Auto-downloads Chrome/Firefox drivers |
| Selenium Manager | built-in | Auto-manages the Edge driver |
| TestNG | 7.9.0 | Test runner, parallel threads, soft asserts |
| Cucumber | 7.15.0 | BDD framework + Gherkin |
| Cucumber TestNG | 7.15.0 | Cucumber–TestNG bridge |
| Apache POI | 5.2.5 | Excel `.xlsx` read/write |
| ExtentReports | 5.1.1 | HTML test report with dashboard |
| extentreports-cucumber7-adapter | 1.14.0 | Auto-wires Extent to Cucumber events |
| Allure | 2.25.0 | Interactive HTML report (`allure-cucumber7-jvm`) |
| Log4j2 | 2.22.0 | Logging to console + rolling file |
| Commons IO | 2.15.1 | File copy helpers for screenshots |

---

## Prerequisites

- Java 11+ (`java -version`)
- Maven 3.6+ (`mvn -version`)
- Chrome **or** Microsoft Edge installed (Edge is the default)
- Internet connection (tests run against the live Coursera site)

---

## Project Structure

```
CourseraAutomation/
├── pom.xml                                ← Maven dependencies & plugins
├── testng.xml                             ← TestNG suite (parallel="classes", thread-count=3)
├── allure.properties                      ← Allure results directory
├── README.md
│
├── src/main/java/com/hackathonProject/
│   ├── base/
│   │   └── BaseClass.java                 ← ThreadLocal<WebDriver>, Chrome/Edge/Firefox launch
│   ├── constants/
│   │   └── FrameworkConstants.java        ← BASE_URL, EXCEL_OUTPUT_PATH, paths
│   ├── pages/                             ← Page Object Model
│   │   ├── HomePage.java
│   │   ├── SearchResultsPage.java         ← incl. CourseInfo inner class
│   │   ├── LanguageCoursesPage.java
│   │   └── CampusPage.java
│   ├── hooks/
│   │   └── CucumberHooks.java             ← @Before launches browser, @After screenshots + quit
│   ├── listeners/
│   │   └── CucumberListener.java          ← ConcurrentEventListener, archives reports
│   └── utils/
│       ├── ConfigReader.java              ← Loads config.properties on first access
│       ├── ExcelDataWriter.java           ← writeCourseData + readTestData
│       ├── WaitUtil.java                  ← explicit & fluent waits (no Thread.sleep)
│       ├── JavaScriptUtil.java            ← JS scroll, JS click, highlight, getText
│       ├── ScreenshotUtil.java            ← File + byte[] capture
│       └── ExtentReportManager.java       ← ThreadLocal<ExtentTest>, timestamped HTML
│
├── src/main/resources/
│   ├── config/config.properties           ← browser, baseUrl, waits, output paths
│   └── log4j2.xml                         ← Console + RollingFile (logs/automation.log)
│
├── src/test/java/com/hackathonProject/
│   ├── runners/
│   │   └── TestRunner.java                ← @CucumberOptions, @DataProvider(parallel=true), tags="@Smoke"
│   └── stepdefinitions/
│       ├── CourseSearchSteps.java
│       ├── LanguageLearningSteps.java
│       └── EnterpriseFormSteps.java
│
├── src/test/resources/
│   ├── features/
│   │   ├── CourseSearch.feature
│   │   ├── LanguageLearning.feature
│   │   └── EnterpriseForm.feature
│   ├── testdata/                          ← Excel output saved here
│   └── extent.properties                  ← Extent Cucumber adapter config
│
├── screenshots/                           ← Captured on failure (and at key form steps)
├── reports/
│   ├── extent/
│   │   ├── ExtentReport.html              ← from the Cucumber-Extent adapter
│   │   └── ExtentReport_<timestamp>.html  ← from ExtentReportManager
│   ├── cucumber/
│   │   ├── cucumber-report.html           ← latest
│   │   ├── cucumber-report_<timestamp>.html  ← archived per run
│   │   ├── cucumber-report.json
│   │   └── cucumber-report_<timestamp>.json
│   └── allure-report/index.html           ← auto-generated by allure-maven
└── logs/
    └── automation.log                     ← rolling daily / 10MB
```

---

## How to Run

### Run the full suite (parallel, 3 threads)
```bash
cd CourseraAutomation
mvn clean test
```
This reads `testng.xml` (via Surefire), which launches `TestRunner` with `parallel="classes"` and `thread-count="3"`. Each Cucumber scenario runs on its own thread because the runner exposes `@DataProvider(parallel = true)`.

Alternatively, from Eclipse: right-click `testng.xml` → **Run As → TestNG Suite**.

### Run only one flow by tag
The runner has `tags="@Smoke"` hard-coded. To restrict further at the CLI:
```bash
mvn test -Dcucumber.filter.tags="@CourseSearch"
mvn test -Dcucumber.filter.tags="@LanguageLearning"
mvn test -Dcucumber.filter.tags="@EnterpriseForm"
```

### Switch browsers
The browser comes from `src/main/resources/config/config.properties` (currently `browser=edge`). To change it:

- **Permanent:** edit `config.properties` → `browser=chrome` (or `firefox`).
- **Per-thread (cross-browser run):** uncomment the `@BeforeClass` block in `TestRunner.java`, define `<parameter name="browser" .../>` blocks in `testng.xml`, and the runner will call `BaseClass.setBrowserOverride(browser)` on each thread.

### Allure report
The `allure-maven` plugin is bound to the `test` phase and auto-generates the report into `reports/allure-report/` after every `mvn test`. To regenerate manually:
```bash
mvn allure:report
# Opens via a static server:
mvn allure:serve
```
Allure HTML can't be opened by double-clicking the file because it loads JSON via AJAX — use the bundled CLI (`.allure/allure-2.27.0/bin/allure serve target/allure-results`) or `mvn allure:serve`.

---

## Reports & Artifacts

| Output | Path |
|--------|------|
| Cucumber HTML | `reports/cucumber/cucumber-report.html` (+ timestamped copy) |
| Cucumber JSON | `reports/cucumber/cucumber-report.json` (+ timestamped copy) |
| Extent HTML (adapter) | `reports/extent/ExtentReport.html` |
| Extent HTML (manager) | `reports/extent/ExtentReport_<timestamp>.html` |
| Allure HTML | `reports/allure-report/index.html` |
| Log file | `logs/automation.log` |
| Excel output | `src/test/resources/testdata/CourseData.xlsx` |
| Screenshots | `screenshots/<scenario>_<timestamp>.png` |

---

## Key Concepts Implemented

| Concept | Where |
|---------|-------|
| BDD (Given/When/Then) | All `.feature` files |
| Page Object Model | `pages/` |
| ThreadLocal WebDriver | `BaseClass.java` |
| Parallel Execution | `testng.xml` + `@DataProvider(parallel = true)` |
| Cross-Browser (Chrome, Edge, Firefox) | `BaseClass.createDriver()` + `browserOverride` ThreadLocal |
| Edge per-thread profile | Unique `--user-data-dir` per thread to avoid session collisions |
| Apache POI (Excel) | `ExcelDataWriter.writeCourseData / readTestData` |
| Custom Cucumber Listener | `CucumberListener` (`ConcurrentEventListener`) — logs steps + archives reports |
| Log4j2 Logging | All classes + `log4j2.xml` (console + RollingFile) |
| Screenshot on Failure | `CucumberHooks.@After` + `ScreenshotUtil` (file + byte[]) |
| Extent Reports | `ExtentReportManager` + `extentreports-cucumber7-adapter` |
| Allure Reports | `allure-cucumber7-jvm` plugin + `allure-maven` |
| Locators | `@FindBy` (id, name, css, xpath) and `By` constants for dynamic content |
| Exception Handling | Try-catch in page methods and waits |
| Soft Assertions | TestNG `SoftAssert` in every step definition |
| DataTable | `EnterpriseFormSteps` form data |
| Config-Driven | `config.properties` + `ConfigReader` |

---

## Notes

- **Never touch `WebDriver` from step definitions** — always go through a Page Object.
- **ThreadLocal is crucial for parallel runs** — without it, threads share a driver and crash.
- **Cucumber hooks live in `CucumberHooks`**, not in the runner.
- **Extent must be flushed once** — `CucumberListener.onRunFinished()` handles it.
- **`log4j2.xml` is at the root of `src/main/resources/`** so Log4j picks it up from the classpath without extra config.
- **Edge needs a unique profile per thread** — set automatically in `BaseClass` using `java.io.tmpdir` + thread id + timestamp. Without this, parallel Edge sessions share user data and intermittently fail.
- **Wait strategy:** zero `Thread.sleep` anywhere; only `WebDriverWait` and `FluentWait` (see `WaitUtil`).
- **Reports are timestamped** so reruns don't overwrite history.