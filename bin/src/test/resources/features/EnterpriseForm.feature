# ============================================================
# Feature: Enterprise / Business Form Validation
# Flow 3 - Go to Coursera For Business
#           Fill "Ready to learn more?" contact form
#           Enter an INVALID email (missing @)
#           Capture and assert the error message
# ============================================================

@EnterpriseForm
Feature: Business Contact Form - Invalid Email Validation

  @Smoke @EnterpriseForm
  Scenario: Verify email validation error on business contact form
    Given the user navigates to the Coursera For Business page
    When the user clicks on Contact Sales
    And the user fills the contact form with the following details:
      | firstName | lastName | email                  | phone      |
      | Srijita   | Baksi    | srijitacogniznat.com   | 9775985472 |
    Then an email validation error message should be displayed
    And the error message should contain "valid email"
