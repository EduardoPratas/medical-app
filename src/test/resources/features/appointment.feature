Feature: Add Appointment to Patient Record
  As a clinician using the medical app
  I want to add appointments to a patient's record
  So that the patient's care history is accurately tracked

  Background:
    Given a patient named "Oliver Hartley" exists in the system

  Scenario: Successfully add an appointment to a patient record
    When I navigate to the appointment form for "Oliver Hartley"
    And I fill in the appointment details:
      | field           | value            |
      | doctorName      | Dr. Smith        |
      | reason          | Annual checkup   |
      | status          | SCHEDULED        |
      | appointmentDate | 2026-06-15T10:00 |
    And I click Save
    Then I should be redirected to the patient record page
    And the appointment with reason "Annual checkup" should appear in the record

  Scenario: Multiple appointments are displayed newest first
    When I add an appointment on "2025-01-10T09:00" with doctor "Dr. Jones"
    And I add an appointment on "2026-06-15T10:00" with doctor "Dr. Smith"
    Then the first appointment in the list should be for "Dr. Smith"
    And the second appointment in the list should be for "Dr. Jones"