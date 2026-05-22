# ============================================================
# Feature: Language Learning
# Flow 2 - Navigate to Language Learning section
#           Extract all available languages with course counts
#           Extract all level options with course counts
#           Display / assert the results
# ============================================================

@LanguageLearning
Feature: Language Learning - Extract Languages and Levels

  @Smoke @LanguageLearning
  Scenario: Extract all languages and levels from Language Learning category
    Given the user navigates to the Language Learning category
    When the user opens the language filter dropdown
    And the user opens the level filter dropdown
    Then all available languages with their counts should be captured and displayed
    And all available levels with their counts should be captured and displayed
    And the language list should contain at least 5 languages
    And the level list should contain at least 2 levels
