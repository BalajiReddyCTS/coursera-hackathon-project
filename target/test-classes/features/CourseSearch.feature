# ============================================================
# Feature: Course Search
# Flow 1 - Search for web development courses (Beginner, English)
#           Extract first 5 course names, hours, ratings
#           Save to Excel using Apache POI
# ============================================================

@CourseSearch
Feature: Course Search - Web Development Courses

  # Background runs before EACH scenario in this feature
  Background:
    Given the user is on the Coursera home page

  @Smoke @CourseSearch
  Scenario: Search web development courses for beginners in English and save to Excel
    When the user searches for "web development courses for beginners"
    And the user applies the language filter "English"
    And the user applies the level filter "Beginner"
    And the user extracts the first 5 courses with name, hours and rating
    And the course data is saved to an Excel file
    Then the search results page should be displayed
    And the first course name should not be empty