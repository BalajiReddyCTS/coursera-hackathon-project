package base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import utils.ConfigReader;

import java.time.Duration;

/**
 * BaseClass - Foundation of the entire framework.
 *
 * KEY CONCEPT: ThreadLocal<WebDriver>
 * When tests run in parallel, each thread needs its OWN browser instance.
 * ThreadLocal ensures each thread gets its own copy of WebDriver.
 * Without it, Thread-1 might accidentally use Thread-2's browser!
 */
public class BaseClass {

    // Logger for this class (Log4j2)
    private static final Logger logger = LogManager.getLogger(BaseClass.class);

    /**
     * ThreadLocal stores one WebDriver per thread.
     * Thread-1 gets driver1, Thread-2 gets driver2 — they never mix.
     */
    private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    /**
     * createDriver() - Called in @Before hook to launch browser.
     * Reads browser name from config.properties.
     * Uses WebDriverManager to auto-download the correct driver binary.
     */
    public static void createDriver() {
        // Read which browser to launch from config
        String browser = ConfigReader.getProperty("browser");
        logger.info("Launching browser: " + browser);

        WebDriver webDriver;

        // Switch on browser name — supports multi-browser execution
        switch (browser.toLowerCase()) {

            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                webDriver = new FirefoxDriver();
                logger.info("Firefox browser launched");
                break;

            case "edge":
                WebDriverManager.edgedriver().setup();
                webDriver = new EdgeDriver();
                logger.info("Edge browser launched");
                break;

            case "chrome":
            default:
                // ChromeOptions lets us customize Chrome behavior
                ChromeOptions options = new ChromeOptions();
                // options.addArguments("--headless"); // Uncomment for headless (no UI)
                options.addArguments("--start-maximized");
                options.addArguments("--disable-notifications");
                WebDriverManager.chromedriver().setup();
                webDriver = new ChromeDriver(options);
                logger.info("Chrome browser launched");
                break;
        }

        // Set implicit wait — Selenium waits up to N seconds before throwing NoSuchElementException
        webDriver.manage().timeouts().implicitlyWait(
            Duration.ofSeconds(Integer.parseInt(ConfigReader.getProperty("implicitWait")))
        );

        // Store this driver in the current thread's slot
        driver.set(webDriver);
        logger.info("WebDriver created and stored in ThreadLocal");
    }

    /**
     * getDriver() - Called everywhere we need to interact with the browser.
     * Returns the WebDriver for the CURRENT thread only.
     */
    public static WebDriver getDriver() {
        return driver.get();
    }

    /**
     * removeDriver() - Called in @After hook to quit browser and clean up.
     * VERY IMPORTANT: always remove from ThreadLocal to prevent memory leaks!
     */
    public static void removeDriver() {
        if (driver.get() != null) {
            logger.info("Quitting browser and removing from ThreadLocal");
            driver.get().quit();
            driver.remove(); // Prevents memory leak — removes the thread's slot
        }
    }
}
