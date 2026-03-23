Feature: FHIR Patient Import
  As a clinician using the medical app
  I want to import patients from a FHIR server
  So that existing patient records are available without manual data entry

  Background:
    Given the FHIR server is running with 5 sample patients

  Scenario: Successfully import new patients from FHIR server
    Given there are no patients in the local database
    When I navigate to the FHIR import page
    And I click the "Run FHIR Import" button
    Then I should see 5 patients were imported
    And I should see 0 patients were skipped
    And all 5 patients should appear in the patient list

  Scenario: Skip patients who already exist in the database
    Given a patient with email "oliver.hartley@nhs.net" already exists in the database
    When I navigate to the FHIR import page
    And I click the "Run FHIR Import" button
    Then I should see 4 patients were imported
    And I should see 1 patient was skipped
    And the message "Skipped 'Oliver Hartley' — already exists." should appear

  Scenario: Running import twice does not create duplicate patients
    Given there are no patients in the local database
    When I click the "Run FHIR Import" button
    And I click the "Run FHIR Import" button again
    Then there should be exactly 5 patients in the database