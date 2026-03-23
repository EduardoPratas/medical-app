package com.medicalapp.acceptance;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.medicalapp.model.Appointment;
import com.medicalapp.model.Patient;
import com.medicalapp.repository.AppointmentRepository;
import com.medicalapp.repository.PatientRepository;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class AppointmentSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private MvcResult lastResult;

    private Patient getCurrentPatient(String name) {
        return patientRepository.findByEmail(SharedSteps.toEmail(name))
                .orElseThrow(() -> new RuntimeException("Patient not found: " + name));
    }

    private Patient getAnyPatient() {
        return patientRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No patient found in database"));
    }

    @When("I navigate to the appointment form for {string}")
    public void iNavigateToTheAppointmentFormFor(String name) throws Exception {
        Patient patient = getCurrentPatient(name);
        lastResult = mockMvc.perform(get("/patients/" + patient.getId() + "/appointments/new"))
                .andExpect(status().isOk())
                .andReturn();
    }

    @When("I fill in the appointment details:")
    public void iFillInTheAppointmentDetails(DataTable dataTable) throws Exception {
        Map<String, String> fields = dataTable.asMap(String.class, String.class);
        Patient patient = getAnyPatient();
        lastResult = mockMvc.perform(post("/patients/" + patient.getId() + "/appointments")
                        .param("doctorName", fields.get("doctorName"))
                        .param("reason", fields.get("reason"))
                        .param("status", fields.get("status"))
                        .param("appointmentDate", fields.get("appointmentDate")))
                .andReturn();
    }

    @And("I click Save")
    public void iClickSave() {
        // Save was triggered in the previous step's POST
    }

    @Then("I should be redirected to the patient record page")
    public void iShouldBeRedirectedToThePatientRecordPage() {
        assertThat(lastResult.getResponse().getStatus()).isBetween(300, 399);
        assertThat(lastResult.getResponse().getRedirectedUrl()).contains("/record");
    }

    @And("the appointment with reason {string} should appear in the record")
    public void theAppointmentWithReasonShouldAppearInTheRecord(String reason) {
        Patient patient = getAnyPatient();
        List<Appointment> appointments = appointmentRepository
                .findByPatientOrderByAppointmentDateDesc(patient);
        assertThat(appointments).anyMatch(a -> reason.equals(a.getReason()));
    }

    @When("I add an appointment on {string} with doctor {string}")
    public void iAddAnAppointmentOnWithDoctor(String date, String doctor) throws Exception {
        Patient patient = getAnyPatient();
        mockMvc.perform(post("/patients/" + patient.getId() + "/appointments")
                        .param("doctorName", doctor)
                        .param("reason", "Test")
                        .param("status", "SCHEDULED")
                        .param("appointmentDate", date))
                .andReturn();
    }

    @Then("the first appointment in the list should be for {string}")
    public void theFirstAppointmentInTheListShouldBeFor(String doctor) {
        Patient patient = getAnyPatient();
        List<Appointment> appointments = appointmentRepository
                .findByPatientOrderByAppointmentDateDesc(patient);
        assertThat(appointments.get(0).getDoctorName()).isEqualTo(doctor);
    }

    @And("the second appointment in the list should be for {string}")
    public void theSecondAppointmentInTheListShouldBeFor(String doctor) {
        Patient patient = getAnyPatient();
        List<Appointment> appointments = appointmentRepository
                .findByPatientOrderByAppointmentDateDesc(patient);
        assertThat(appointments.get(1).getDoctorName()).isEqualTo(doctor);
    }
}