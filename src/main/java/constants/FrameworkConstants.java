package constants;

/**
 * FrameworkConstants - stores all hard-coded values used across the framework.
 *
 * WHY: Avoids magic strings/numbers scattered in code.
 * Change a URL or path here and it updates everywhere.
 */
public class FrameworkConstants {

    // ===== URLs =====
    public static final String BASE_URL = "https://www.coursera.org";
    public static final String FOR_BUSINESS_URL = "https://www.coursera.org/business";
    public static final String LANGUAGE_SEARCH_URL =
        "https://www.coursera.org/search?query=language+learning";

    // ===== File Paths =====
    public static final String EXCEL_OUTPUT_PATH =
        "src/test/resources/testdata/CourseData.xlsx";
    public static final String SCREENSHOT_PATH = "screenshots/";
    public static final String EXTENT_REPORT_PATH = "reports/extent/ExtentReport.html";

    // ===== Test Data =====
    public static final String SEARCH_QUERY = "web development courses for beginners";
    public static final String LANGUAGE_FILTER = "English";
    public static final String LEVEL_FILTER = "Beginner";
    public static final int COURSES_TO_EXTRACT = 10;

    // ===== Form Data =====
    public static final String FORM_FIRST_NAME = "Srijita";
    public static final String FORM_LAST_NAME = "Baksi";
    public static final String FORM_INVALID_EMAIL = "srijitacogniznat.com"; // Missing @
    public static final String FORM_PHONE = "9775985472";

    // ===== Timeouts =====
    public static final int IMPLICIT_WAIT_SECONDS = 10;
    public static final int EXPLICIT_WAIT_SECONDS = 15;
    public static final int PAGE_LOAD_TIMEOUT_SECONDS = 30;

    // Private constructor — this is a utility class, not meant to be instantiated
    private FrameworkConstants() {}
}
