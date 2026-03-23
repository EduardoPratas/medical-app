Feature: Delete Patient
  As a clinician using the medical app
  I want to delete a patient from the system
  So that the patient list only contains current active patients

  Scenario: Successfully delete a patient
    Given a patient named "Arthur Blackwood" exists in the system
    When I delete the patient "Arthur Blackwood"
    Then I should be redirected to the patient list
    And "Arthur Blackwood" should no longer appear in the patient list

  Scenario: Deleting one patient does not affect other patients
    Given a patient named "Arthur Blackwood" exists in the system
    And a patient named "Evelyn Marsden" exists in the system
    When I delete the patient "Arthur Blackwood"
    Then "Evelyn Marsden" should still appear in the patient list

  Scenario: Deleting a patient also removes their appointments
    Given a patient named "Oliver Hartley" exists in the system
    And "Oliver Hartley" has an appointment with "Dr. Smith"
    When I delete the patient "Oliver Hartley"
    Then there should be no appointments in the database for "Oliver Hartley"