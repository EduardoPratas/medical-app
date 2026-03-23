package com.medicalapp.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.medicalapp.model.Patient;
import com.medicalapp.repository.PatientRepository;
import com.medicalapp.service.FhirImportService;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class FhirImportSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private FhirImportService fhirImportService;

    @LocalServerPort
    private int port;

    private MvcResult lastResult;
    private FhirImportService.FhirImportResult importResult;

    @Before
    public void setUp() {
        patientRepository.deleteAll();
        // Point FhirImportService at the real embedded server port for this test run
        ReflectionTestUtils.setField(fhirImportService, "fhirServerUrl",
                "http://localhost:" + port + "/fhir");
    }

    @Given("the FHIR server is running with 5 sample patients")
    public void theFhirServerIsRunningWithSamplePatients() {
        // FhirDataStore loads patients automatically on Spring context startup
    }

    @Given("there are no patients in the local database")
    public void thereAreNoPatientsInTheLocalDatabase() {
        patientRepository.deleteAll();
        assertThat(patientRepository.count()).isZero();
    }

    @Given("a patient with email {string} already exists in the database")
    public void aPatientAlreadyExistsInTheDatabase(String email) {
        Patient existing = new Patient();
        existing.setName("Oliver Hartley");
        existing.setEmail(email);
        existing.setPhone("07700900001");
        patientRepository.save(existing);
    }

    @When("I navigate to the FHIR import page")
    public void iNavigateToTheFhirImportPage() throws Exception {
        lastResult = mockMvc.perform(get("/fhir/import"))
                .andExpect(status().isOk())
                .andReturn();
    }

    @When("I click the {string} button")
    public void iClickTheButton(String buttonLabel) {
        importResult = fhirImportService.importPatients();
    }

    @When("I click the {string} button again")
    public void iClickTheButtonAgain(String buttonLabel) {
        importResult = fhirImportService.importPatients();
    }

    @Then("I should see {int} patients were imported")
    public void iShouldSeePatientsWereImported(int count) {
        assertThat(importResult.getImported()).isEqualTo(count);
    }

    @Then("I should see {int} patients were skipped")
    public void iShouldSeePatientsWereSkipped(int count) {
        assertThat(importResult.getSkipped()).isEqualTo(count);
    }

    @Then("I should see {int} patient was skipped")
    public void iShouldSeeOnePatientWasSkipped(int count) {
        assertThat(importResult.getSkipped()).isEqualTo(count);
    }

    @And("all {int} patients should appear in the patient list")
    public void allPatientsShouldAppearInThePatientList(int count) {
        assertThat(patientRepository.count()).isEqualTo(count);
    }

    @And("the message {string} should appear")
    public void theMessageShouldAppear(String message) {
        assertThat(importResult.getMessages()).anyMatch(m -> m.contains(message));
    }

    @Then("there should be exactly {int} patients in the database")
    public void thereShouldBeExactlyPatientsInTheDatabase(int count) {
        assertThat(patientRepository.count()).isEqualTo(count);
    }
}