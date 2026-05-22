# ============================================================
# Feature: Enterprise / Business Form Validation
# Flow 3 - Go to Coursera For Business
#           Fill "Ready to learn more?" contact form
#           Enter an INVALID email (missing @)
#           Capture and assert the error message
#
# NOTE: Coursera's form shows different error messages:
# - "This field is required." for empty fields
# - "Must be valid email. example@yourdomain.com" for invalid email
# - "Please enter a valid email address" (HTML5 validation)
# The assertion checks for "valid email" which covers the
# invalid-email case, OR we also accept "required" if the form
# validation triggers a required-field error.
# ============================================================

@EnterpriseForm
Feature: Business Contact Form - Invalid Email Validation

  @Smoke @EnterpriseForm
  Scenario: Verify email validation error on business contact form
    Given the user navigates to the Coursera For Business page
    When the user clicks on Contact Sales
    And the user fills the contact form with the following details:
      | firstName | lastName | email                  |
      | Srijita   | Baksi    | srijitacogniznat.com   | 
    Then an email validation error message should be displayed
    And the error message should contain "valid email"
