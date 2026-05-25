# CourseraAutomation — Complete Project Guide



---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [What This Project Demonstrates](#2-what-this-project-demonstrates)
3. [Tech Stack Deep Dive](#3-tech-stack-deep-dive)
4. [Project Structure](#4-project-structure)
5. [The Three Test Scenarios](#5-the-three-test-scenarios)
6. [End-to-End Execution Flow](#6-end-to-end-execution-flow)
7. [Core Framework Components](#7-core-framework-components)
8. [Maven Build Configuration](#8-maven-build-configuration)
9. [Jenkins CI/CD Pipeline](#9-jenkins-cicd-pipeline)
10. [Reporting Strategy](#10-reporting-strategy)
11. [Key Concepts Quick Reference](#11-key-concepts-quick-reference)
12. [Likely Interview Questions and Answers](#12-likely-interview-questions-and-answers)
13. [How to Run](#13-how-to-run)

---

## 1. Executive Summary

**CourseraAutomation** is a behaviour-driven (BDD) Selenium automation framework that exercises three real user journeys on Coursera.org. It demonstrates production-grade automation practices including the Page Object Model, parallel execution via ThreadLocal, cross-browser support, soft assertions, structured logging, triple reporting, and CI/CD integration with Jenkins.

| Aspect | Implementation |
|--------|---------------|
| **Test framework** | Cucumber 7 BDD with TestNG as the runner |
| **Browser automation** | Selenium WebDriver 4 |
| **Parallel strategy** | TestNG `parallel="classes"` + DataProvider parallelism + ThreadLocal<WebDriver> |
| **Browsers supported** | Chrome, Edge, Firefox |
| **Reporting** | Cucumber HTML + Extent + Allure |
| **CI/CD** | Jenkins declarative pipeline |
| **Build tool** | Maven |

### One-line elevator pitch

> *"I built a BDD framework using Cucumber, Selenium and TestNG that runs three real Coursera user journeys in parallel across browsers, with soft assertions, triple reporting and a Jenkins pipeline for continuous regression."*

---

## 2. What This Project Demonstrates


| # | Capability | Where It Lives |
|---|------------|----------------|
| 1 | **BDD with Gherkin** — readable Given/When/Then scenarios | `.feature` files |
| 2 | **Page Object Model** — UI structure separated from test logic | `pages/` package |
| 3 | **Thread-safe parallel execution** | `BaseClass` + `testng.xml` + `@DataProvider(parallel=true)` |
| 4 | **Cross-browser execution** | `BaseClass.createDriver()` with Chrome/Edge/Firefox |
| 5 | **Smart explicit & fluent waits** (zero `Thread.sleep`) | `WaitUtil.java` |
| 6 | **Data-driven testing via DataTable & Excel** | `EnterpriseFormSteps` + `ExcelDataWriter` |
| 7 | **Soft assertions** — continue on failure | TestNG `SoftAssert` in every step definition |
| 8 | **Exception handling** | Try-catch in page methods, FluentWait `.ignoring(...)` |
| 9 | **Structured logging** with rolling files | Log4j2 with `log4j2.xml` |
| 10 | **Triple reporting** (Cucumber + Extent + Allure) | Plugins in `TestRunner` + `ExtentReportManager` |
| 11 | **Screenshots on failure**, attached to reports | `CucumberHooks.@After` + `ScreenshotUtil` |
| 12 | **Custom Cucumber event listener** | `CucumberListener` (ConcurrentEventListener) |
| 13 | **Externalised configuration** | `config.properties` + `ConfigReader` |
| 14 | **CI/CD via Jenkins** | `Jenkinsfile` declarative pipeline |

---

## 3. Tech Stack Deep Dive

Understanding *why* each library was chosen — and what it replaces — is one of the most common interview questions.

### Languages & Build

| Tool | Version | Why It's Used |
|------|---------|---------------|
| **Java** | 25       | Industry-standard LTS version; supports lambdas, streams, var, modern HTTP client. Most automation jobs use Java 8 or 11. |
| **Maven** | 3.6+    | Dependency management, build lifecycle (compile → test → package), plugin ecosystem (Surefire, Allure). |

### Automation Core

| Tool | Version | Purpose |
|------|---------|---------|
| **Selenium WebDriver** | 4.18.1 | Drives the browser via the W3C WebDriver protocol. Selenium 4 brought relative locators, Selenium Manager, and a clean BiDi API. |
| **WebDriverManager** | 5.7.0 | Auto-downloads matching `chromedriver`/`geckodriver` binaries based on installed browser version. Replaces manual driver-version juggling. |
| **Selenium Manager** | built-in to Selenium 4.6+ | Same role as WebDriverManager but built into Selenium itself. Used for Edge in this project. |

### Test Runner

| Tool | Version | Purpose |
|------|---------|---------|
| **TestNG** | 7.9.0 | Provides `@Test`, parallel execution, assertions, listeners, data providers. Chosen over JUnit because it natively supports parallel-class execution and richer XML configuration. |
| **Cucumber JVM** | 7.15.0 | BDD framework that parses `.feature` files (Gherkin) and matches each step to a Java method. |
| **cucumber-testng** | 7.15.0 | Bridges Cucumber to TestNG so we get TestNG's parallel + reporting capabilities. |

### Data & I/O

| Tool | Version | Purpose |
|------|---------|---------|
| **Apache POI** | 5.2.5 | Reads and writes Excel `.xlsx` files. Used for course data export. |
| **commons-io** | 2.15.1 | `FileUtils.copyFile()` for screenshot saving. |

### Reporting

| Tool | Version | Purpose |
|------|---------|---------|
| **ExtentReports** | 5.1.1 | Polished dark-themed HTML dashboard with charts. |
| **extentreports-cucumber7-adapter** | 1.14.0 | Auto-wires Extent to Cucumber events; no manual hook code needed. |
| **Allure** | 2.25.0 | Interactive HTML report with trends, history, severity, behaviours, defects view. |
| **allure-cucumber7-jvm** | 2.25.0 | Adapter that emits Allure JSON during Cucumber runs. |
| **allure-maven** | 2.14.0 | Maven plugin that auto-generates the Allure HTML after `mvn test`. |

### Logging

| Tool | Version | Purpose |
|------|---------|---------|
| **Log4j2** | 2.22.0 | Async-capable logging. Console + rolling file appenders. Configured via `log4j2.xml`. |

> **Interview tip:** When asked "Why TestNG over JUnit?", answer: *Parallel-class support out of the box, richer XML suite config, native soft assertions, parameterised tests via `@DataProvider`, and built-in listeners.* When asked "Why Cucumber?", answer: *BDD lets business stakeholders read and write feature files in plain English — Given/When/Then — which makes requirements traceable to tests.*

---

## 4. Project Structure

```
CourseraAutomation/
├── pom.xml                                  ← Maven dependencies & plugins
├── testng.xml                               ← TestNG suite definition (parallel config)
├── allure.properties                        ← Allure results directory
├── Jenkinsfile                              ← Jenkins declarative pipeline
├── .gitignore                               ← Files excluded from Git
├── README.md
│
├── src/main/java/com/hackathonProject/
│   ├── base/
│   │   └── BaseClass.java                   ← WebDriver factory + ThreadLocal
│   ├── constants/
│   │   └── FrameworkConstants.java          ← Project-wide constants
│   ├── pages/                               ← Page Object Model
│   │   ├── HomePage.java
│   │   ├── SearchResultsPage.java
│   │   ├── LanguageCoursesPage.java
│   │   └── CampusPage.java
│   ├── hooks/
│   │   └── CucumberHooks.java               ← @Before/@After per scenario
│   ├── listeners/
│   │   └── CucumberListener.java            ← Event-driven test logging
│   └── utils/
│       ├── ConfigReader.java                ← Loads config.properties
│       ├── ExcelDataWriter.java             ← Apache POI Excel I/O
│       ├── WaitUtil.java                    ← Explicit & Fluent waits
│       ├── JavaScriptUtil.java              ← JavascriptExecutor helpers
│       ├── ScreenshotUtil.java              ← Failure screenshots
│       └── ExtentReportManager.java         ← Extent singleton + ThreadLocal
│
├── src/main/resources/
│   ├── config/config.properties             ← Browser, URL, timeouts
│   └── log4j2.xml                           ← Logging configuration
│
├── src/test/java/com/hackathonProject/
│   ├── runners/
│   │   └── TestRunner.java                  ← @CucumberOptions + TestNG bridge
│   └── stepdefinitions/
│       ├── CourseSearchSteps.java
│       ├── LanguageLearningSteps.java
│       └── EnterpriseFormSteps.java
│
└── src/test/resources/
    ├── features/
    │   ├── CourseSearch.feature
    │   ├── LanguageLearning.feature
    │   └── EnterpriseForm.feature
    ├── testdata/                            ← Excel output saved here
    └── extent.properties                    ← Extent adapter config
```

### Maven Standard Directory Layout

Notice the strict `src/main/java`, `src/main/resources`, `src/test/java`, `src/test/resources` separation. This is **Maven Standard Directory Layout** and is critical:

- `src/main/*` — production code that goes into the final JAR
- `src/test/*` — code that only runs during tests
- `*/java` — Java source files
- `*/resources` — non-Java files (XML, properties) placed on the classpath

If you put `log4j2.xml` in the wrong folder, Log4j won't find it. That's a common pitfall and a likely interview question.

---

## 5. The Three Test Scenarios

### Flow 1 — Course Search and Excel Export
**Feature:** `CourseSearch.feature` | **Tag:** `@CourseSearch`, `@Smoke`

```gherkin
Scenario: Search web development courses for beginners in English and save to Excel
  When the user searches for "web development courses for beginners"
  And the user applies the language filter "English"
  And the user applies the level filter "Beginner"
  And the user extracts the first 5 courses with name, hours and rating
  And the course data is saved to an Excel file
  Then the search results page should be displayed
  And the first course name should not be empty
```

**What it does:** Searches Coursera, applies filters via URL manipulation, scrapes the top 5 course cards (name + rating + duration), and saves them to `CourseData.xlsx` using Apache POI.

**Why URL filtering instead of clicking?** Coursera's filter dropdowns behave inconsistently between Chrome and Edge. Mutating the URL (`?language=English&productDifficultyLevel=Beginner`) is deterministic across browsers and faster.

### Flow 2 — Language and Level Extraction
**Feature:** `LanguageLearning.feature` | **Tag:** `@LanguageLearning`, `@Smoke`

```gherkin
Scenario: Extract all languages and levels from Language Learning category
  Given the user navigates to the Language Learning category
  When the user opens the language filter dropdown
  And the user opens the level filter dropdown
  Then all available languages with their counts should be captured and displayed
  And all available levels with their counts should be captured and displayed
  And the language list should contain at least 5 languages
  And the level list should contain at least 2 levels
```

**What it does:** Opens the Language Learning category, expands the Language filter and captures every language with its course count, reloads the page, expands the Level filter and captures every level.

**Why reload between extractions?** After extracting 60+ language labels, both Language and Level filters can briefly remain visible. A clean reload (`driver.navigate().to(PAGE_URL)`) resets the filter panel.

### Flow 3 — Business Contact Form Validation
**Feature:** `EnterpriseForm.feature` | **Tag:** `@EnterpriseForm`, `@Smoke`

```gherkin
Scenario: Verify email validation error on business contact form
  Given the user navigates to the Coursera For Business page
  When the user clicks on Contact Sales
  And the user fills the contact form with the following details:
    | firstName | lastName | email                  |
    | Srijita   | Baksi    | srijitacogniznat.com   |
  Then an email validation error message should be displayed
  And the error message should contain "valid email"
```

**What it does:** Navigates to the For Business page, fills the Marketo-embedded contact form with an invalid email (no `@`), submits, and asserts that the validation error message contains "valid email".

**Why fill email last?** Marketo's inline error tooltip overlaps adjacent fields the moment Email loses focus. Filling First Name → Last Name → Email avoids `ElementClickInterceptedException`.

---

## 6. End-to-End Execution Flow

This is the **single most important diagram to internalise**. When you run `mvn clean test`, here's what happens, in order:

```
┌─────────────────────────────────────────────────────────────────┐
│  1. mvn clean test                                              │
│         │                                                       │
│         ▼                                                       │
│  2. Surefire Plugin reads testng.xml                            │
│         │  (parallel="classes", thread-count="3")               │
│         ▼                                                       │
│  3. TestNG instantiates TestRunner                              │
│         │  (extends AbstractTestNGCucumberTests)                │
│         ▼                                                       │
│  4. @DataProvider(parallel=true) returns one row per scenario   │
│         │                                                       │
│         ▼                                                       │
│  5. TestNG dispatches each scenario onto a separate thread      │
│     ┌────────────┬────────────┬────────────┐                    │
│     │ Thread-1   │ Thread-2   │ Thread-3   │                    │
│     ▼            ▼            ▼                                 │
│  6. CucumberHooks.@Before runs:                                 │
│     - BaseClass.createDriver()                                  │
│     - ExtentReportManager.createTest()                          │
│         │                                                       │
│         ▼                                                       │
│  7. Cucumber parses .feature file -> matches steps to           │
│     @Given/@When/@Then methods                                  │
│         │                                                       │
│         ▼                                                       │
│  8. Step definition method runs:                                │
│     - logs the step                                             │
│     - calls a Page Object method                                │
│     - Page Object performs WebDriver actions                    │
│     - SoftAssert collects results                               │
│         │                                                       │
│         ▼                                                       │
│  9. CucumberListener fires for every TestStepFinished           │
│     and logs PASS / FAIL / SKIP                                 │
│         │                                                       │
│         ▼                                                       │
│  10. CucumberHooks.@After runs:                                 │
│      - If failed: capture screenshot (bytes + file)             │
│      - Attach to Cucumber + Extent reports                      │
│      - BaseClass.removeDriver() (always)                        │
│         │                                                       │
│         ▼                                                       │
│  11. After all scenarios: CucumberListener.onRunFinished()      │
│      - Flushes Extent Reports to disk                           │
│      - Archives Cucumber reports with timestamp                 │
│         │                                                       │
│         ▼                                                       │
│  12. allure-maven plugin generates HTML from                    │
│      target/allure-results/ into reports/allure-report/         │
└─────────────────────────────────────────────────────────────────┘
```

Memorise this. In a code walkthrough, being able to narrate steps 1–12 is what separates a strong candidate from a junior one.

---

## 7. Core Framework Components

### 7.1 BaseClass.java — Driver Management & Thread Safety

**Location:** `src/main/java/com/hackathonProject/base/BaseClass.java`

**Purpose:** The factory and lifecycle manager for `WebDriver` instances. The single most important class for parallel safety.

#### Key Code Walkthrough

```java
private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();
private static ThreadLocal<String> browserOverride = new ThreadLocal<>();
```

**Why `ThreadLocal`?**
`ThreadLocal<T>` is a Java utility that gives each thread its own private copy of a variable. When Thread-1 calls `driver.set(chromeDriver1)`, Thread-2 cannot see it. Each thread reads only its own slot.

Without `ThreadLocal`, three parallel scenarios would all share **one** WebDriver. They'd race, click the wrong elements, and crash with `NoSuchSessionException` or worse.

**Mental model:** Imagine a locker room — `ThreadLocal` gives each thread its own locker. The variable name is shared (`driver`), but the contents inside each locker are private.

```java
public static String getCurrentBrowser() {
    String override = browserOverride.get();
    if (override != null) return override;
    String sysProp = System.getProperty("browser");
    if (sysProp != null && !sysProp.isEmpty()) return sysProp;
    return ConfigReader.getProperty("browser");
}
```

**Three-tier precedence:**
1. **ThreadLocal override** — for cross-browser parallel runs (Thread-1 gets Chrome, Thread-2 gets Edge)
2. **System property** (`-Dbrowser=chrome` on the Maven command line) — for Jenkins/CI overrides
3. **config.properties file** — the default value

This pattern is called the **Configuration Precedence Pattern**. CI overrides config, runtime overrides CI, but defaults are sensible.

```java
public static void createDriver() {
    String browser = getCurrentBrowser();
    WebDriver webDriver;

    if (browser.equalsIgnoreCase("chrome")) {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--start-maximized");
        chromeOptions.addArguments("--disable-notifications");
        chromeOptions.addArguments("--no-sandbox");
        chromeOptions.addArguments("--disable-dev-shm-usage");
        WebDriverManager.chromedriver().setup();
        webDriver = new ChromeDriver(chromeOptions);
    }
    ...
}
```

**Chrome flags explained:**

| Flag | Purpose |
|------|---------|
| `--start-maximized` | Opens the browser maximised so coordinate-based clicks work |
| `--disable-notifications` | Suppresses "Allow notifications?" popups that would block clicks |
| `--no-sandbox` | Required when running as root (e.g. inside Docker / Jenkins) |
| `--disable-dev-shm-usage` | Forces Chrome to use `/tmp` instead of `/dev/shm` (which is too small in containers and causes crashes) |

**Why `equalsIgnoreCase`?** So the user can put `browser=Chrome` or `browser=CHROME` in `config.properties` without breaking.

```java
String uniqueProfile = System.getProperty("java.io.tmpdir")
    + "edge_profile_" + Thread.currentThread().threadId()
    + "_" + System.currentTimeMillis();
edgeOptions.addArguments("--user-data-dir=" + uniqueProfile);
```

**Why a unique profile for Edge?**
Microsoft Edge serialises access to its default user-data directory. If two threads both launch Edge against the default profile, the second one fails with *"user data directory is already in use"*. Combining `tmpdir + threadId + timestamp` gives every thread a guaranteed-unique directory.

```java
webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(...));
webDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(...));
```

**Implicit wait vs page-load timeout:**

| Setting | What It Waits For |
|---------|-------------------|
| `implicitlyWait` | Polls the DOM up to N seconds while looking for an element via `findElement` |
| `pageLoadTimeout` | How long `driver.get(url)` and `navigate().to()` are allowed to take before throwing |

```java
public static void removeDriver() {
    if (driver.get() != null) {
        try { driver.get().quit(); }
        catch (Exception e) { logger.warn(...); }
        driver.remove();
        browserOverride.remove();
    }
}
```

**Why `try-catch` around `quit()`?** The browser may have already crashed. Better to log a warning than throw an exception that masks the original test failure.

**Why `driver.remove()`?** Threads in TestNG's thread pool get reused. If we don't clear the ThreadLocal slot, the next scenario on that thread might inherit a dead reference.

#### Likely Questions on BaseClass

> **Q: What is ThreadLocal and why did you use it?**
> ThreadLocal provides each thread with its own private copy of a variable. We use it for WebDriver because parallel scenarios must not share a single browser instance — that would cause races and crashes. Each thread's `driver.get()` returns the WebDriver that *that* thread set.

> **Q: What happens if you forget `driver.remove()`?**
> Memory leak. TestNG reuses threads from a pool. If the ThreadLocal slot is never cleared, the dead WebDriver reference stays bound to the thread for the entire JVM lifetime. Over many runs in a CI server, this builds up.

> **Q: Why WebDriverManager instead of manually setting `webdriver.chrome.driver`?**
> WebDriverManager auto-detects the installed Chrome version and downloads the matching driver. The manual approach breaks every time Chrome auto-updates.

---

### 7.2 ConfigReader.java — Configuration Management

**Location:** `src/main/java/com/hackathonProject/utils/ConfigReader.java`

**Purpose:** Centralised, lazy-loaded reader for `config.properties`. Loads once, serves forever.

```java
private static Properties properties = new Properties();

static {
    try {
        String configPath = "src/main/resources/config/config.properties";
        FileInputStream fis = new FileInputStream(configPath);
        properties.load(fis);
        fis.close();
    } catch (IOException e) {
        throw new RuntimeException("config.properties not found: " + e.getMessage());
    }
}
```

**Three key concepts here:**

1. **`static` block** — runs **once**, the first time the class is referenced anywhere in the JVM. Used here to load the file exactly once, no matter how many times `ConfigReader.getProperty()` is called later.

2. **`Properties` class** — a `java.util.Properties` is essentially a `HashMap<String, String>` with built-in `load()` and `store()` methods for the `.properties` file format (`key=value` pairs).

3. **Failing fast** — if the file is missing, we throw a `RuntimeException` immediately rather than letting later code crash with confusing NullPointerExceptions.

```java
public static String getProperty(String key) {
    String value = properties.getProperty(key);
    if (value == null || value.isEmpty()) {
        throw new RuntimeException("Missing property: " + key);
    }
    return value.trim();
}
```

**Why `.trim()`?** Defensive programming — if someone accidentally writes `browser= chrome ` with stray spaces, the trim prevents subtle bugs (the `equalsIgnoreCase("chrome")` check in BaseClass would fail).

#### Design Pattern: Singleton (informal)

`ConfigReader` is an **informal singleton** — there's no instance at all because everything is `static`. This is called the **Utility/Helper class pattern**. The class isn't instantiated; you just call static methods on it.

#### Likely Questions

> **Q: Why is the loading code in a static block instead of a method?**
> Static blocks run exactly once at class-load time. If we put it in `getProperty()`, we'd reload the file on every call (or need extra null-check logic). The static block is the cleanest "load once, use forever" idiom.

> **Q: What happens if two threads call `ConfigReader.getProperty()` simultaneously?**
> Safe. The static initialiser runs under a JVM-level lock, so the `Properties` object is fully populated before any thread can call `getProperty()`. Reads from `Properties` are also thread-safe.

---

### 7.3 FrameworkConstants.java — Centralised Constants

```java
public class FrameworkConstants {
    public static final String BASE_URL = ConfigReader.getProperty("baseUrl");
    public static final String EXCEL_OUTPUT_PATH = ConfigReader.getProperty("excelOutputPath");
    public static final String SCREENSHOT_PATH = "screenshots/";
    public static final String EXTENT_REPORT_PATH = "reports/extent/ExtentReport.html";

    private FrameworkConstants() {}
}
```

**Three Java fundamentals here:**

1. **`public static final`** — the constant trio:
    - `public` — accessible from anywhere
    - `static` — belongs to the class, not an instance
    - `final` — cannot be reassigned after initialisation

2. **Private constructor** — prevents anyone from instantiating the class. `FrameworkConstants` is a pure namespace for constants, not a thing you create. The private constructor is a deliberate "you shall not pass" sign.

3. **Field initialisers run at class-load time** — `BASE_URL = ConfigReader.getProperty("baseUrl")` is executed when `FrameworkConstants` is first referenced.

---

### 7.4 CucumberHooks.java — Scenario Lifecycle

**Location:** `src/main/java/com/hackathonProject/hooks/CucumberHooks.java`

**Purpose:** Cucumber's equivalent of TestNG's `@BeforeMethod` / `@AfterMethod`. Runs before and after every scenario.

```java
@Before
public void setUp(Scenario scenario) {
    BaseClass.createDriver();
    ExtentReportManager.createTest(scenario.getName());
    ExtentReportManager.logInfo("Browser launched for scenario: " + scenario.getName());
}
```

**Important:** This `@Before` is `io.cucumber.java.Before`, **not** TestNG's `@BeforeMethod` or JUnit's `@Before`. They're three different annotations with the same name in different packages.

The `Scenario` parameter is injected by Cucumber and gives you access to the scenario name, tags, status, and an `.attach()` method for attaching files to the report.

```java
@After
public void tearDown(Scenario scenario) {
    if (scenario.isFailed()) {
        byte[] screenshotBytes = ScreenshotUtil.captureScreenshotAsBytes(BaseClass.getDriver());
        if (screenshotBytes != null) {
            scenario.attach(screenshotBytes, "image/png", "Failure Screenshot");
        }
        String screenshotPath = ScreenshotUtil.captureScreenshot(
            BaseClass.getDriver(), scenario.getName()
        );
        ExtentReportManager.logFail("Scenario FAILED: " + scenario.getName());
        ExtentReportManager.attachScreenshot(screenshotPath);
    } else {
        ExtentReportManager.logPass("Scenario PASSED: " + scenario.getName());
    }
    BaseClass.removeDriver();
}
```

**Why capture screenshots two ways?**

| Method | Goes To |
|--------|---------|
| `captureScreenshotAsBytes()` -> `scenario.attach()` | Cucumber HTML report (embedded inline as base64) |
| `captureScreenshot()` -> file path -> Extent | Extent Report (linked from disk) |

**Why `removeDriver()` runs always, not only on success?**
Browsers are heavyweight processes. Forgetting to quit them leaks memory and OS handles. The hook unconditionally cleans up regardless of test outcome.

#### Likely Questions

> **Q: Where do you put cleanup code in Cucumber?**
> In an `@After` hook in a class on the glue path (we use `hooks.CucumberHooks`). Cucumber discovers it via the `glue` parameter of `@CucumberOptions`.

> **Q: Difference between `@Before` and `@BeforeStep`?**
> `@Before` runs once per scenario. `@BeforeStep` runs before each individual Gherkin step within a scenario.

---

### 7.5 CucumberListener.java — Event-Driven Logging

**Location:** `src/main/java/com/hackathonProject/listeners/CucumberListener.java`

**Purpose:** Subscribes to Cucumber's event bus and emits structured log entries. Also archives reports with timestamps after the run.

```java
public class CucumberListener implements ConcurrentEventListener {

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestCaseStarted.class, this::onScenarioStart);
        publisher.registerHandlerFor(TestStepFinished.class, this::onStepFinished);
        publisher.registerHandlerFor(TestCaseFinished.class, this::onScenarioFinished);
        publisher.registerHandlerFor(TestRunFinished.class, this::onRunFinished);
    }
}
```

**Three Java concepts here:**

1. **Interface implementation** — `implements ConcurrentEventListener` means this class provides the methods Cucumber expects (`setEventPublisher`). The `@Override` annotation tells the compiler "I'm overriding a parent method" — if the parent signature changes, compilation fails (catches typos).

2. **Method references** — `this::onScenarioStart` is a method reference, shorthand for the lambda `event -> this.onScenarioStart(event)`. It's a Java 8+ feature for cleaner functional code.

3. **Event-driven design** — Cucumber publishes events as the run progresses. We subscribe to specific event types. This is the **Observer pattern**: the subject (Cucumber) notifies observers (our listener) when state changes.

**Why "Concurrent"EventListener?**
Cucumber has two listener interfaces: `EventListener` (single-threaded) and `ConcurrentEventListener` (thread-safe). Since our scenarios run in parallel, we **must** use the concurrent version, or we'd serialise event handling and lose parallelism gains.

```java
private void onStepFinished(TestStepFinished event) {
    if (event.getTestStep() instanceof PickleStepTestStep) {
        PickleStepTestStep step = (PickleStepTestStep) event.getTestStep();
        String stepText = step.getStep().getText();
        Result result = event.getResult();

        switch (result.getStatus()) {
            case PASSED:  logger.info("STEP PASSED: " + stepText); break;
            case FAILED:  logger.error("STEP FAILED: " + stepText); break;
            case SKIPPED: logger.warn("STEP SKIPPED: " + stepText); break;
        }
    }
}
```

**Why `instanceof PickleStepTestStep`?**
Cucumber fires `TestStepFinished` for both **Pickle steps** (your actual Gherkin steps) and **Hook steps** (the @Before/@After hooks). The `instanceof` check filters to just the Gherkin steps so we don't double-log.

```java
private void onRunFinished(TestRunFinished event) {
    ExtentReportManager.flushReports();
    archiveCucumberReports();
}
```

**Flushing Extent Reports** — this is mandatory. Without `flush()`, the in-memory report is never written to disk and the HTML file stays empty.

---

### 7.6 Page Object Layer

**Purpose:** Encapsulate page-level UI knowledge so test code stays clean.

#### What is Page Object Model?

POM is a design pattern where **each web page becomes a class**. The class:

- Declares the page's elements as fields (using `@FindBy` or `By` constants)
- Exposes methods that perform user actions on those elements (`searchFor()`, `fillForm()`)
- Hides all WebDriver code from the test/step definition layer

#### Benefits

| Benefit | Why It Matters |
|---------|----------------|
| **Single point of change** | If the search box `id` changes, you update *one* line in `HomePage.java` |
| **Readable tests** | Step definitions read like user stories, not Selenium calls |
| **Reusability** | The same `searchFor()` method serves all scenarios that search |
| **Separation of concerns** | Test logic (what) vs UI mechanics (how) |

#### 7.6.1 HomePage.java

```java
public class HomePage {
    private WebDriver driver;

    @FindBy(name = "query")
    private WebElement searchBox;

    @FindBy(xpath = "//button[@aria-label='Close']")
    private WebElement closePopupBtn;

    public HomePage() {
        this.driver = BaseClass.getDriver();
        PageFactory.initElements(driver, this);
    }
}
```

**What's `@FindBy` and `PageFactory`?**
The `@FindBy` annotation is Selenium's way of declaratively binding a field to a locator. `PageFactory.initElements(driver, this)` walks every `@FindBy`-annotated field via reflection and proxies it so that **when you first call a method on the field, Selenium does `findElement` at that moment** (lazy lookup).

This means you can write `searchBox.click()` and Selenium will locate the element behind the scenes — no manual `driver.findElement(By.name("query")).click()`.

**Why pass `BaseClass.getDriver()` in the constructor?**
This pulls the **current thread's** WebDriver from the ThreadLocal. Each parallel scenario gets its own HomePage instance bound to its own driver.

```java
public void searchFor(String searchTerm) {
    dismissPopup();
    WaitUtil.waitForElementVisible(driver, searchBox);
    searchBox.click();
    searchBox.clear();
    searchBox.sendKeys(searchTerm);
    searchBox.sendKeys(Keys.ENTER);
    WaitUtil.waitForPageLoad(driver);
    dismissPopup();
}
```

**Why click before clear before sendKeys?**
Some sites bind their JS handlers to focus events. Clicking first ensures focus. `clear()` empties any pre-filled text. `sendKeys()` types the new search term.

#### 7.6.2 SearchResultsPage.java — Pattern Matching with Regex

```java
private static final Pattern RATING_PATTERN = Pattern.compile(
    "(\\d\\.\\d)\\s*·\\s*[\\d.,]+[Kk]?\\s*reviews?"
);
```

This regex parses strings like `4.8 · 12,345 reviews`:
- `(\\d\\.\\d)` — captures a digit, dot, digit (the rating, e.g. `4.8`)
- `\\s*·\\s*` — the middle-dot separator with optional whitespace
- `[\\d.,]+` — one or more digits, dots or commas (the review count)
- `[Kk]?` — optional 'K' for thousands
- `reviews?` — "review" with optional 's'

**Why two patterns?** `RATING_FALLBACK` handles cards where the review count isn't present — it just grabs any `X.X` number.

#### 7.6.3 @FindBy vs By — A Critical Distinction

| Approach | When to Use |
|----------|-------------|
| `@FindBy` + `PageFactory` | **Static** elements that exist at initial page load (search box, form fields) |
| `By` constants + `driver.findElement(locator)` | **Dynamic** elements that appear after navigation, reload, or filter changes |

**Why?** `@FindBy` elements become stale after `driver.navigate().to(...)`. `By` constants re-locate the element on each call.

`LanguageCoursesPage` uses `By` constants because it reloads the page mid-scenario. `HomePage` uses `@FindBy` because its elements are stable.

---

### 7.7 Utility Layer

#### 7.7.1 WaitUtil.java — The Three Wait Strategies

This is **the** classic interview topic. Memorise the three wait types cold.

| Wait Type | What It Does | Example |
|-----------|--------------|---------|
| **Implicit** | Set once on the driver; applies to *every* `findElement` call | `driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10))` |
| **Explicit** | Waits for a specific condition (visibility, clickability) on a specific element | `new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.visibilityOf(element))` |
| **Fluent** | Like explicit but with polling interval and exception-ignoring | `new FluentWait<>(driver).withTimeout(...).pollingEvery(...).ignoring(NoSuchElementException.class)` |

```java
public static WebElement waitForElement(WebDriver driver, By locator, int timeoutSec) {
    try {
        return new FluentWait<>(driver)
            .withTimeout(Duration.ofSeconds(timeoutSec))
            .pollingEvery(Duration.ofMillis(500))
            .ignoring(NoSuchElementException.class)
            .ignoring(StaleElementReferenceException.class)
            .until(d -> {
                WebElement el = d.findElement(locator);
                return el.isDisplayed() ? el : null;
            });
    } catch (Exception e) {
        return null;
    }
}
```

**Line-by-line:**
- `FluentWait<>(driver)` — generic class parameterised over the input type
- `.withTimeout(...)` — maximum wait
- `.pollingEvery(...)` — check every 500ms
- `.ignoring(...)` — don't fail on these expected exceptions; keep retrying
- `.until(d -> ...)` — a lambda that returns the element when ready or null/false to keep polling
- `try-catch` returning `null` — lets callers ask "does this exist?" without crashing

**Why prefer explicit over implicit?**

| Implicit Wait | Explicit Wait |
|---------------|---------------|
| Applies to every `findElement` blindly | Targeted to a specific condition |
| Can't wait for state (visible/clickable) | Can wait for any `ExpectedCondition` |
| **Combining with explicit causes unpredictable timeouts** | Composable and clear |

The classic rule: **do not mix implicit and explicit waits.** If implicit is set to 10s and explicit to 5s, the actual wait can be anywhere between 5s and 15s depending on the driver implementation.

**Why no `Thread.sleep`?** A `Thread.sleep(3000)` always waits 3 seconds, even if the element appears in 200 ms. Across many steps, this wastes minutes. Explicit waits return *as soon as the condition is met*.

```java
public static void briefPause(WebDriver driver, int millis) {
    try {
        new FluentWait<>(driver)
            .withTimeout(Duration.ofMillis(millis))
            .pollingEvery(Duration.ofMillis(millis))
            .until(d -> false);
    } catch (Exception ignored) {}
}
```

**The `briefPause` trick:**
`.until(d -> false)` is a condition that never becomes true. The FluentWait times out, the exception is swallowed, and the net effect is a clean N-millisecond pause **without using `Thread.sleep`**. It's syntactic purity for the "no Thread.sleep" policy.

#### 7.7.2 JavaScriptUtil.java — When Native Selenium Fails

```java
public static void scrollAndClick(WebDriver driver, WebElement element) {
    JavascriptExecutor js = (JavascriptExecutor) driver;
    js.executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
    WaitUtil.briefPause(driver, 500);
    js.executeScript("arguments[0].click();", element);
}
```

**Why JavaScript click instead of `element.click()`?**

Selenium's native `click()` checks for overlapping elements. If a popup, banner, or sticky header covers your target, it throws `ElementClickInterceptedException`. JavaScript's `element.click()` **bypasses overlap checks** — it just fires the click event.

**When to use which:**
- **Native click** — default. Catches real UX bugs (you wouldn't be able to click as a human either).
- **JS click** — for known-overlap situations like Marketo forms or dismissible banners.

**The `arguments[0]` syntax:** Selenium's `executeScript(script, args...)` passes Java objects as `arguments[0]`, `arguments[1]`, etc. into the JS context. So `arguments[0]` refers to the `element` we passed in.

#### 7.7.3 ExcelDataWriter.java — Apache POI

```java
try (XSSFWorkbook workbook = new XSSFWorkbook()) {
    XSSFSheet sheet = workbook.createSheet("Web Dev Courses");

    Row headerRow = sheet.createRow(1);
    headerRow.createCell(0).setCellValue("S.No");
    headerRow.createCell(1).setCellValue("Course Name");

    try (FileOutputStream fos = new FileOutputStream(filePath)) {
        workbook.write(fos);
    }
}
```

**Three Java idioms:**

1. **`try-with-resources`** — `try (XSSFWorkbook wb = ...)` automatically calls `wb.close()` when the try block exits, even on exception. Eliminates resource-leak bugs.

2. **POI's class hierarchy:**
    - `XSSF*` — for `.xlsx` (XML-based, Excel 2007+)
    - `HSSF*` — for `.xls` (binary, old Excel)
    - `SXSSF*` — streaming variant for huge files

3. **0-indexed everything** — `createRow(0)` is the first row, `createCell(0)` is column A.

#### 7.7.4 ExtentReportManager.java — Singleton + ThreadLocal

```java
private static ExtentReports extentReports;
private static ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

private static synchronized ExtentReports getExtentReports() {
    if (extentReports == null) {
        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
        extentReports = new ExtentReports();
        extentReports.attachReporter(sparkReporter);
    }
    return extentReports;
}
```

**Why `synchronized`?**
Three threads might call `getExtentReports()` simultaneously. Without `synchronized`, all three could see `extentReports == null`, all three could create new instances, and we'd end up with three reports overwriting each other. The `synchronized` keyword ensures only one thread enters the method at a time.

This is the **double-checked locking idiom**, simplified. The pattern is **lazy singleton**.

**Why `ThreadLocal<ExtentTest>`?**
Each scenario gets its own ExtentTest node, but they all log to the **same** ExtentReports instance. Multiple test nodes inside one report = thread-local. One report singleton = static.

---

### 7.8 Step Definitions

Step definitions are the **glue** between Gherkin and Java. Each step in a `.feature` file is matched to a method via regex/cucumber-expressions in the `@Given`/`@When`/`@Then` annotations.

```java
@When("the user searches for {string}")
public void theUserSearchesFor(String searchTerm) {
    logger.info("STEP: Searching for: " + searchTerm);
    ExtentReportManager.logInfo("Searching for: " + searchTerm);
    homePage.searchFor(searchTerm);
}
```

**Cucumber Expressions vs Regex:**
- `{string}` — built-in Cucumber Expression that matches anything in quotes
- `{int}` — matches integers
- `{word}` — matches a single word
- Custom regex like `@When("user clicks (.*) button")` also works

**The Three-Line Pattern**
Every step definition in this framework follows the same three-line rhythm:

```java
1. logger.info(...);                       // Log4j2 - file/console
2. ExtentReportManager.logInfo(...);       // Extent dashboard
3. <pageObject>.<userAction>(...);         // Delegate to POM
```

This **never** changes. Steps don't manipulate WebDriver, don't do assertions on raw elements, don't compute things. They only orchestrate.

#### Soft Assertions

```java
private SoftAssert softAssert = new SoftAssert();

@Then("the first course name should not be empty")
public void theFirstCourseNameShouldNotBeEmpty() {
    softAssert.assertFalse(
        extractedCourses == null || extractedCourses.isEmpty(),
        "Courses list is empty"
    );
    softAssert.assertAll();
}
```

**Hard vs Soft Assertion:**

| | Hard (`Assert`) | Soft (`SoftAssert`) |
|---|---|---|
| **Failure behaviour** | Throws immediately, stops test | Records failure, test continues |
| **All checks run?** | No | Yes |
| **When to use** | Critical preconditions (driver is null) | Multiple non-blocking validations in one scenario |
| **Reporting** | One failure per test | All failures together at `assertAll()` |

The key: `softAssert.assertAll()` is called **only at the last step** of each scenario, after all soft asserts have been recorded.

#### DataTable

```java
@When("the user fills the contact form with the following details:")
public void theUserFillsContactForm(DataTable dataTable) {
    List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
    Map<String, String> formData = rows.get(0);
    String firstName = formData.get("firstName");
}
```

**Gherkin source:**
```gherkin
And the user fills the contact form with the following details:
  | firstName | lastName | email                  |
  | Srijita   | Baksi    | srijitacogniznat.com   |
```

Cucumber converts the table into Java automatically. `.asMaps()` treats the first row as headers and produces a `List<Map<String, String>>`.

---

### 7.9 TestRunner.java — Cucumber/TestNG Bridge

```java
@CucumberOptions(
    features = "src/test/resources/features",
    glue = {
        "com.hackathonProject.stepdefinitions",
        "com.hackathonProject.hooks"
    },
    plugin = {
        "pretty",
        "html:reports/cucumber/cucumber-report.html",
        "json:reports/cucumber/cucumber-report.json",
        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
        "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:",
        "com.hackathonProject.listeners.CucumberListener"
    },
    tags = "@Smoke",
    monochrome = true,
    publish = false
)
public class TestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
```

**Every parameter explained:**

| Parameter | Meaning |
|-----------|---------|
| `features` | Path to `.feature` files |
| `glue` | Packages Cucumber should scan for step definitions and hooks |
| `plugin = "pretty"` | Pretty console output with colours |
| `plugin = "html:..."` | Write Cucumber HTML report to this path |
| `plugin = "json:..."` | Write Cucumber JSON for further tooling |
| `plugin = "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"` | Emit Allure JSON during execution |
| `plugin = "com.aventstack...ExtentCucumberAdapter:"` | Activate Extent adapter (note trailing `:`) |
| `plugin = "...CucumberListener"` | Our custom listener |
| `tags = "@Smoke"` | Run only scenarios tagged `@Smoke` |
| `monochrome = true` | Cleaner console output (no ANSI escape codes) |
| `publish = false` | Don't auto-upload reports to Cucumber.io |

**Why extend `AbstractTestNGCucumberTests`?**
This abstract class is the official Cucumber-TestNG bridge. It exposes a `@DataProvider` called `scenarios()` that returns one row per `.feature` scenario. By overriding it with `@DataProvider(parallel = true)`, we tell TestNG: "run each scenario row on its own thread."

---

### 7.10 Feature Files — Gherkin

```gherkin
@CourseSearch
Feature: Course Search - Web Development Courses

  Background:
    Given the user is on the Coursera home page

  @Smoke @CourseSearch
  Scenario: Search web development courses for beginners in English and save to Excel
    When the user searches for "web development courses for beginners"
    And the user applies the language filter "English"
```

**Gherkin keywords:**

| Keyword | Purpose |
|---------|---------|
| `Feature` | Top-level group; one per file |
| `Background` | Steps run before *every* scenario in this feature |
| `Scenario` | Single test case |
| `Scenario Outline` | Parameterised scenarios with `Examples:` table |
| `Given` | Preconditions / setup |
| `When` | Actions |
| `Then` | Expected outcomes |
| `And` / `But` | Continuation of the previous keyword |
| `@tag` | Categorise scenarios; usable in `tags` filter |

**Why `Background`?**
Every Course Search scenario needs the home page open first. Putting it in `Background` saves repetition.

**Tag hierarchy:**
- `@Smoke` — fast critical-path tests, run on every commit
- `@CourseSearch`, `@LanguageLearning`, `@EnterpriseForm` — feature-specific filters

---

## 8. Maven Build Configuration

### Maven Coordinates

```xml
<groupId>com.coursera</groupId>
<artifactId>CourseraAutomation</artifactId>
<version>1.0-SNAPSHOT</version>
<packaging>jar</packaging>
```

**The three coordinates uniquely identify a project in any Maven repository.** Together, they form the GAV (Group, Artifact, Version) — the "primary key" in the Maven world. `1.0-SNAPSHOT` means "version 1.0 currently in development" (vs `1.0` which would be a released artifact).

### Maven Build Lifecycle

When you run `mvn clean test`, Maven executes a sequence of *phases*:

```
clean -> validate -> compile -> test-compile -> test -> package -> install -> deploy
```

We use only:
- **`clean`** — deletes `target/` (fresh start)
- **`compile`** — compiles `src/main/java/`
- **`test-compile`** — compiles `src/test/java/`
- **`test`** — runs Surefire (which runs TestNG -> Cucumber -> our suite)

### Surefire Configuration

```xml
<plugin>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.1.2</version>
    <configuration>
        <parallel>classes</parallel>
        <threadCount>3</threadCount>
        <suiteXmlFiles>
            <suiteXmlFile>testng.xml</suiteXmlFile>
        </suiteXmlFiles>
        <systemPropertyVariables>
            <allure.results.directory>
                ${project.build.directory}/allure-results
            </allure.results.directory>
        </systemPropertyVariables>
    </configuration>
</plugin>
```

**What this does:**
- Tells Surefire to read `testng.xml` for test discovery
- Sets a system property `allure.results.directory` pointing to `target/allure-results/` so Allure adapters know where to write
- The `parallel`/`threadCount` here is partially redundant because `testng.xml` also defines them, but it's belt-and-braces

### Allure Maven Plugin

```xml
<plugin>
    <groupId>io.qameta.allure</groupId>
    <artifactId>allure-maven</artifactId>
    <version>2.14.0</version>
    <executions>
        <execution>
            <phase>test</phase>
            <goals><goal>report</goal></goals>
        </execution>
    </executions>
</plugin>
```

**Binding a goal to a phase** — `<phase>test</phase>` means "run the `report` goal whenever the `test` phase runs". So after every `mvn test`, Allure auto-regenerates the HTML.

### testng.xml

```xml
<suite name="CourseraAutomationSuite" parallel="classes" thread-count="3">
    <test name="CourseraTests" preserve-order="true">
        <classes>
            <class name="com.hackathonProject.runners.TestRunner"/>
        </classes>
    </test>
</suite>
```

**Parallel modes in TestNG:**

| Mode | Granularity |
|------|-------------|
| `methods` | Each `@Test` method on its own thread |
| `classes` | Each test class on its own thread |
| `tests` | Each `<test>` block on its own thread |
| `instances` | Each instance of a class on its own thread |

We use `classes` because each Cucumber scenario becomes an "instance" of TestRunner via the parallel data provider.

---

## 9. Jenkins CI/CD Pipeline

### What Is CI/CD?

| Term | Meaning |
|------|---------|
| **CI** (Continuous Integration) | Every code commit automatically triggers a build + tests |
| **CD** (Continuous Delivery) | Successful builds are automatically packaged and ready to deploy |
| **CD** (Continuous Deployment) | Successful builds are automatically deployed to production |

This project does CI: every push runs the suite, publishes reports, archives artifacts.

### The Jenkinsfile

```groovy
pipeline {
    agent any

    tools {
        jdk 'JDK25'
        maven 'Maven3'
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
    }

    environment {
        MAVEN_OPTS = '-Xmx1024m'
    }

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }
        stage('Tool Verification') {
            steps {
                bat 'java -version'
                bat 'mvn -version'
            }
        }
        stage('Build') {
            steps { bat 'mvn clean compile -B' }
        }
        stage('Test') {
            steps { bat 'mvn test -B -Dbrowser=chrome -Dheadless=true' }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
            archiveArtifacts artifacts: 'reports/**/*, target/surefire-reports/**/*, screenshots/**/*',
                             allowEmptyArchive: true
        }
        success { echo 'Build SUCCESS' }
        failure { echo 'Build FAILED' }
        unstable { echo 'Build UNSTABLE - some tests failed' }
    }
}
```

### Line-by-Line

**`pipeline { ... }`** — declarative pipeline syntax (Jenkins 2.x+). The newer, structured alternative to the older "scripted pipeline" (which uses Groovy code directly).

**`agent any`** — run on any available Jenkins agent (including the controller). For larger setups you'd say `agent { label 'windows-selenium' }` to target a specific agent.

**`tools { jdk 'JDK25'; maven 'Maven3' }`** — references tools configured in **Manage Jenkins -> Tools**. Jenkins puts them on `PATH` for this build. The names must match exactly.

**`options`:**
- `timeout(30, MINUTES)` — kills the build if it hangs past 30 minutes
- `buildDiscarder(logRotator(numToKeepStr: '10'))` — keeps only the last 10 builds; older ones are auto-deleted
- `timestamps()` — prepends `[2026-05-22T12:34:56]` to each console line (requires Timestamper plugin)

**`environment { MAVEN_OPTS = '-Xmx1024m' }`** — sets `MAVEN_OPTS` for the build. `-Xmx1024m` means "give Maven up to 1 GB of heap memory."

**`stages`:**
- `Checkout` — `checkout scm` pulls the Git repo as configured in the job
- `Tool Verification` — `bat` runs a Windows batch command and fails the build if exit code is not 0
- `Build` — `mvn clean compile -B`. The `-B` flag is "batch mode" — disables Maven's interactive prompts and colour output, ideal for CI logs
- `Test` — runs the actual tests with `-Dbrowser=chrome -Dheadless=true`. The `-D` flag sets a JVM system property, which our `BaseClass.getCurrentBrowser()` reads

**`post { always { ... } }`** — runs whether the build passed or failed:
- `junit testResults: 'target/surefire-reports/*.xml'` — parses Surefire XML reports and shows the TestNG-style pass/fail summary in Jenkins
- `archiveArtifacts artifacts: '...'` — uploads the listed files to Jenkins so you can download them from the build page

### What's NOT in This Pipeline (Yet)

Things you could add as "future improvements" to mention in interviews:
- **Build parameters** (BROWSER, TAGS dropdowns via `parameters { choice(...) }`)
- **HTML Publisher** to make Cucumber/Extent reports clickable from the build page
- **Allure Jenkins plugin** to render the interactive Allure dashboard
- **Email Extension** to notify on failure
- **Nightly schedule** via `triggers { cron('H 22 * * *') }`
- **GitHub webhook** for trigger-on-push
- **Multi-branch pipeline** to run on every PR

### Note on `-Dheadless=true`

The current `BaseClass` reads `-Dbrowser` from `System.getProperty()` but **not** `-Dheadless`. So the `-Dheadless=true` flag in the Jenkinsfile is currently a no-op — Chrome will still launch with a visible window. To enable real headless mode, `BaseClass.createDriver()` needs to read the headless property and call `chromeOptions.addArguments("--headless=new")`. **This is a known enhancement and a good thing to mention as "what I'd improve next."**

---

## 10. Reporting Strategy

### Why Three Reports?

Each report serves a different audience:

| Report | Best For | Strengths |
|--------|----------|-----------|
| **Cucumber HTML** | Developers | Direct view of Gherkin -> pass/fail mapping with embedded screenshots |
| **Extent** | Managers / leads | Dark-themed dashboard with charts, system info, easy to share via email |
| **Allure** | QA leads / stakeholders | Trends over time, severity, defects view, behaviours, history |

### Cucumber HTML Report
- Generated by the `html:` Cucumber plugin
- Path: `reports/cucumber/cucumber-report.html`
- Companion JSON: `reports/cucumber/cucumber-report.json` (machine-readable, used by external tools)

### Extent Report
- **Two outputs** in this framework:
    - From the adapter (configured via `extent.properties`): `reports/extent/ExtentReport.html`
    - From `ExtentReportManager` (manual): `reports/extent/ExtentReport_<timestamp>.html`
- The adapter version is auto-wired to Cucumber events; the manager version receives step-level info from your step definitions

### Allure
- Path: `reports/allure-report/index.html`
- Generated by `allure-maven` after every `mvn test`
- **Important:** Allure HTML can't be opened by double-clicking (it loads JSON via AJAX, which browsers block from `file://`). Use `mvn allure:serve` to start a local server.

### Screenshot Strategy

```java
if (scenario.isFailed()) {
    byte[] screenshotBytes = ScreenshotUtil.captureScreenshotAsBytes(driver);
    scenario.attach(screenshotBytes, "image/png", "Failure Screenshot");
    String screenshotPath = ScreenshotUtil.captureScreenshot(driver, scenario.getName());
    ExtentReportManager.attachScreenshot(screenshotPath);
}
```

**Two formats for two destinations:**
- **Bytes** (base64) embedded in the Cucumber HTML report — survives moving the report file
- **File path** linked from Extent — keeps the report file small but breaks if you move it without the screenshots folder

---

## 11. Key Concepts Quick Reference

### Java Concepts Used

| Concept | Where in the Project |
|---------|----------------------|
| **OOP — encapsulation** | Private fields in page objects, getters where needed |
| **OOP — inheritance** | `TestRunner extends AbstractTestNGCucumberTests` |
| **OOP — polymorphism** | `WebDriver webDriver = new ChromeDriver()` (interface reference, concrete impl) |
| **OOP — abstraction** | `WebDriver` interface hides browser-specific code |
| **Static initialiser** | `ConfigReader`'s static block |
| **Static final constants** | `FrameworkConstants` |
| **Generics** | `ThreadLocal<WebDriver>`, `FluentWait<WebDriver>`, `List<CourseInfo>` |
| **Lambda expressions** | `.until(d -> { ... })` in WaitUtil |
| **Method references** | `this::onScenarioStart` in CucumberListener |
| **Try-with-resources** | `try (XSSFWorkbook wb = ...)` in ExcelDataWriter |
| **Try-catch** | Around `driver.quit()`, in WaitUtil, ScreenshotUtil |
| **Exception handling** | RuntimeException for config failures, swallowed exceptions for popups |
| **Collections — List** | Course extraction returns `List<CourseInfo>` |
| **Collections — Map** | Language -> count mapping via `LinkedHashMap` (preserves insertion order) |
| **Collections — Set** | `seen.add(name)` returns false on duplicates; deduplication idiom |
| **Regex** | `Pattern.compile()` in SearchResultsPage and LanguageCoursesPage |
| **String formatting** | `String.format("%-25s -> %d courses", lang, count)` |
| **Synchronization** | `synchronized` keyword in `ExtentReportManager.getExtentReports()` |
| **Thread safety** | `ThreadLocal` everywhere driver state lives |
| **Enum/Switch** | Switch on `result.getStatus()` in CucumberListener |
| **Annotations** | `@FindBy`, `@Before`, `@After`, `@CucumberOptions`, `@DataProvider`, `@Override` |
| **Reflection** | `PageFactory.initElements()` uses reflection to wire `@FindBy` fields |

### Design Patterns Used

| Pattern | Where |
|---------|-------|
| **Page Object Model** | `pages/` package |
| **Factory Method** | `BaseClass.createDriver()` decides which browser to instantiate |
| **Singleton (lazy)** | `ExtentReportManager.getExtentReports()` |
| **Utility / Helper class** | `ConfigReader`, `WaitUtil`, `JavaScriptUtil`, `ScreenshotUtil` |
| **Observer / Event Listener** | `CucumberListener` subscribes to Cucumber's event bus |
| **Builder (via Selenium Options)** | `new ChromeOptions().addArguments(...)` |

### Selenium Concepts

| Concept | Quick Definition |
|---------|------------------|
| **WebDriver** | Interface for browser control (Chrome/Firefox/Edge implement it) |
| **WebElement** | Represents a DOM element |
| **By** | Locator strategy (id, name, xpath, css, className, linkText, partialLinkText, tagName) |
| **PageFactory** | Selenium utility that wires `@FindBy` fields via reflection + proxy |
| **Actions** | API for keyboard/mouse gestures (drag, hover, double-click) — not used here |
| **JavascriptExecutor** | Cast WebDriver to this to run JS |
| **TakesScreenshot** | Cast WebDriver to this for screenshots |
| **ExpectedConditions** | Library of pre-built conditions for WebDriverWait |

### Selenium Exception Cheat Sheet

| Exception | Cause | Fix |
|-----------|-------|-----|
| `NoSuchElementException` | Element not in DOM at lookup time | Add explicit wait |
| `StaleElementReferenceException` | DOM re-rendered after you got the WebElement reference | Re-locate the element |
| `ElementNotInteractableException` | Element exists but is hidden or disabled | Wait for visibility/clickability |
| `ElementClickInterceptedException` | Another element overlaps the target | JS click or scroll into view |
| `TimeoutException` | A wait condition wasn't met in time | Increase timeout or check selector |
| `WebDriverException` | Generic driver-level failure | Check the message — often a session-died issue |
| `InvalidSelectorException` | Bad CSS/XPath syntax | Fix the selector |
| `NoSuchWindowException` | Switched to a window that doesn't exist | Verify window handles |
| `UnhandledAlertException` | A JavaScript alert is open | Accept or dismiss the alert first |

---

## 12. Likely Interview Questions and Answers

### Project Walkthrough

> **Q: Walk me through your project.**

**Sample answer (~90 seconds):**
> "This is a Selenium BDD framework for Coursera. It tests three flows: course search with Excel export, language/level extraction, and contact form validation. The architecture uses Cucumber for Gherkin-style scenarios, TestNG for the runner and parallel execution, and Selenium 4 for the browser. I followed the Page Object Model — each page is a class with `@FindBy` elements and user-action methods. Driver instances are stored in a ThreadLocal so three scenarios can run in parallel without interfering. I use only explicit and fluent waits — no Thread.sleep anywhere. The framework produces three reports: Cucumber HTML, Extent, and Allure. It's wired into Jenkins via a declarative pipeline that runs on every commit."

### Concept Questions

> **Q: Why ThreadLocal?**
>
> A: To give each parallel-running thread its own private WebDriver instance. Without ThreadLocal, three threads would share one browser and crash with session errors.

> **Q: Difference between implicit, explicit, and fluent wait?**
>
> A:
> - **Implicit** — set once on the driver, applies to every `findElement` call.
> - **Explicit** — `WebDriverWait` until a specific condition on a specific element.
> - **Fluent** — like explicit but with polling interval and exception-ignoring.
    > Best practice: never mix implicit and explicit — the behaviour becomes unpredictable.

> **Q: What is Page Object Model and why use it?**
>
> A: A design pattern where each page is a class containing elements (as fields) and user actions (as methods). Benefits: tests are readable, locators change in one place, methods are reusable.

> **Q: BDD vs TDD?**
>
> A: TDD = Test-Driven Development — developers write tests first, in code. BDD = Behaviour-Driven Development — tests are written in plain-English Gherkin (Given/When/Then) so business stakeholders can read and write them. Cucumber is a BDD tool.

> **Q: Soft assertion vs hard assertion?**
>
> A: Hard (`Assert.assertTrue`) fails immediately and stops the test. Soft (`SoftAssert.assertTrue`) records the failure and continues; you call `assertAll()` at the end to report everything. Use soft for multiple non-blocking validations; hard for critical preconditions.

> **Q: How do you handle dynamic elements?**
>
> A: Use Fluent Wait with retry and exception-ignoring. For elements that change after a navigation, use `By` constants instead of `@FindBy` so they're re-located on every call.

> **Q: How does parallel execution work in this framework?**
>
> A: Three things cooperate. `testng.xml` sets `parallel="classes" thread-count="3"`. `TestRunner` overrides the scenarios DataProvider with `parallel=true` so each Cucumber scenario is a separate row. `BaseClass` uses `ThreadLocal<WebDriver>` so each thread gets its own browser.

> **Q: How do you take a screenshot on failure?**
>
> A: In `CucumberHooks.@After`, check `scenario.isFailed()`. If true, cast WebDriver to `TakesScreenshot`, capture as bytes and attach to the Cucumber report via `scenario.attach()`. Also save as file via `FileUtils.copyFile` and link to Extent via `addScreenCaptureFromPath`.

> **Q: What happens if you forget to call `driver.quit()`?**
>
> A: The browser process stays alive after the test ends. Across many test runs, this leaks memory and OS handles. On Jenkins, after a few hundred runs the agent runs out of memory.

> **Q: What's the difference between `driver.close()` and `driver.quit()`?**
>
> A: `close()` closes the current window; if it's the only window, the session ends. `quit()` closes all windows associated with the WebDriver and ends the session immediately. Always use `quit()` in teardown.

> **Q: What is `JavascriptExecutor` and when do you use it?**
>
> A: An interface that WebDriver implements, exposed via casting. Use it for scrolling, clicking elements that are intercepted by overlays, getting text from hidden elements, or interacting with the page directly when Selenium APIs fall short.

> **Q: How does Cucumber match a Gherkin step to a Java method?**
>
> A: Through the `@Given`/`@When`/`@Then` annotations on step-definition methods. The annotation value is either a Cucumber Expression (like `{string}`) or a regex. At runtime, Cucumber scans the glue packages, builds the mapping, and dispatches each Gherkin line to the matching method.

> **Q: What is the glue path in Cucumber?**
>
> A: The package(s) Cucumber should scan for step definitions and hooks, declared in `@CucumberOptions.glue = {...}`. Without glue, Cucumber can't find the methods.

> **Q: How do you skip a scenario in Cucumber?**
>
> A: Tag it `@Ignored` (or any custom tag) and exclude it via `tags = "not @Ignored"` in `@CucumberOptions` or `-Dcucumber.filter.tags="not @Ignored"`.

> **Q: How does Jenkins know which Maven and JDK to use?**
>
> A: Through the `tools { jdk 'X'; maven 'Y' }` block in the Jenkinsfile. The names must match entries in **Manage Jenkins -> Tools**. Jenkins prepends those tools to `PATH` for the build.

> **Q: Difference between `bat` and `sh` in a Jenkinsfile?**
>
> A: `bat` runs a Windows batch command; `sh` runs a Unix shell command. Use whichever matches the agent OS. For cross-platform pipelines, use `isUnix()` checks or scripts that work in both.

> **Q: What does `-B` mean in `mvn -B`?**
>
> A: Batch mode. Disables Maven's interactive prompts and reduces console colour output. Standard for CI.

> **Q: What is `-Dkey=value` in Maven?**
>
> A: It sets a JVM system property, accessible via `System.getProperty("key")`. We use it in Jenkins to override `browser` without editing `config.properties`.

### Tricky Questions

> **Q: Three threads call `ExtentReportManager.createTest()` simultaneously. What ensures they don't corrupt the report?**
>
> A: Two layers: (1) `ExtentReports` itself is thread-safe for `createTest()`. (2) `getExtentReports()` is `synchronized` so only one thread initialises the singleton. Each thread stores its own `ExtentTest` in `ThreadLocal<ExtentTest>` so they log to separate nodes.

> **Q: Why does `LanguageCoursesPage` use `By` constants but `HomePage` uses `@FindBy`?**
>
> A: `LanguageCoursesPage` reloads the page mid-scenario (`driver.navigate().to(PAGE_URL)`). After a reload, `@FindBy` elements become stale. `By` constants re-locate the element on each call, so they survive reloads.

> **Q: Can you have multiple `@Before` hooks?**
>
> A: Yes. Cucumber runs them in the order declared (or based on the `order = N` parameter). You can have separate hooks for different concerns — one for the driver, one for test data.

> **Q: What if Cucumber finds two step definitions matching the same Gherkin line?**
>
> A: It throws `DuplicateStepDefinitionException` and the run aborts. The fix is to remove the duplicate or make the regex/expression more specific.

> **Q: Why is `monochrome = true` in `@CucumberOptions`?**
>
> A: Cleans the console output by stripping ANSI colour escape codes. Easier to read in CI logs and text files.

---

## 13. How to Run

### Prerequisites

- Java 11+ installed and `JAVA_HOME` set
- Maven 3.6+ on `PATH`
- Chrome or Edge installed
- Internet access (tests hit live Coursera site)

### Local Execution

**Run the full suite:**
```bash
mvn clean test
```

**Override the browser at runtime:**
```bash
mvn clean test -Dbrowser=chrome
mvn clean test -Dbrowser=firefox
```

**Run a specific tag:**
```bash
mvn clean test -Dcucumber.filter.tags="@CourseSearch"
```

**From Eclipse:**
Right-click `testng.xml` -> **Run As -> TestNG Suite**

### Jenkins Execution

1. Push project to GitHub
2. Install Jenkins plugins: Pipeline, Git, HTML Publisher, Timestamper
3. Configure Tools: `JDK25` (JDK), `Maven3` (Maven) — names must match the Jenkinsfile
4. Create a new **Pipeline** job
5. Pipeline definition: **Pipeline script from SCM** -> Git -> your repo URL -> Branch `main` -> Script Path `Jenkinsfile`
6. Save -> **Build Now**

### Viewing Reports

| Report | How to View |
|--------|-------------|
| Cucumber HTML | Double-click `reports/cucumber/cucumber-report.html` |
| Extent HTML | Double-click `reports/extent/ExtentReport.html` |
| Allure HTML | `mvn allure:serve` (Allure can't open directly from `file://`) |
| Log file | Open `logs/automation.log` in any text editor |

### Excel Output

After Flow 1 runs, the extracted course data is at:
```
src/test/resources/testdata/CourseData.xlsx
```

Open it in Excel — you'll see the timestamp, headers, and 5 course rows.

---

## Final Tips for the Evaluation

1. **Know your numbers.** Three flows, three threads, five courses extracted, three reports, eleven Java concepts, six design patterns. Numbers make answers concrete.

2. **Have a one-line answer ready** for every concept. Practise saying "ThreadLocal gives each thread its own private copy of a variable" out loud until it's automatic.

3. **The "why" beats the "what".** Anyone can list `@FindBy`. Few can explain *why* `PageFactory` uses reflection. The latter shows understanding.

4. **Own the trade-offs.** When asked "why did you not use X?", give the trade-off, not a dismissal. *"I use URL-based filtering instead of clicking because the UI behaves differently across browsers"* > *"clicking doesn't work."*

5. **Demonstrate growth mindset.** When asked "what would you improve?", have answers ready: full headless support, GitHub webhook triggers, Selenium Grid for distributed runs, Allure trends across builds, multi-branch pipeline.
