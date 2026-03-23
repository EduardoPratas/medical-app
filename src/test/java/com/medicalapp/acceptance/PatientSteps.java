package com.medicalapp.acceptance;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.medicalapp.model.Appointment;
import com.medicalapp.model.Patient;
import com.medicalapp.repository.AppointmentRepository;
import com.medicalapp.repository.PatientRepository;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class PatientSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private MvcResult lastResult;

    @And("{string} has an appointment with {string}")
    public void patientHasAnAppointmentWith(String patientName, String doctor) {
        Patient patient = patientRepository.findByEmail(SharedSteps.toEmail(patientName)).orElseThrow();
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctorName(doctor);
        appointment.setReason("Checkup");
        appointment.setStatus("SCHEDULED");
        appointment.setAppointmentDate(LocalDateTime.now().plusDays(7));
        appointmentRepository.save(appointment);
    }

    @When("I delete the patient {string}")
    public void iDeleteThePatient(String name) throws Exception {
        Patient patient = patientRepository.findByEmail(SharedSteps.toEmail(name)).orElseThrow();
        lastResult = mockMvc.perform(get("/patients/delete/" + patient.getId()))
                .andReturn();
    }

    @Then("I should be redirected to the patient list")
    public void iShouldBeRedirectedToThePatientList() {
        assertThat(lastResult.getResponse().getStatus()).isBetween(300, 399);
        assertThat(lastResult.getResponse().getRedirectedUrl()).isEqualTo("/patients");
    }

    @And("{string} should no longer appear in the patient list")
    public void patientShouldNoLongerAppear(String name) {
        assertThat(patientRepository.findByEmail(SharedSteps.toEmail(name))).isEmpty();
    }

    @Then("{string} should still appear in the patient list")
    public void patientShouldStillAppear(String name) {
        assertThat(patientRepository.findByEmail(SharedSteps.toEmail(name))).isPresent();
    }

    @Then("there should be no appointments in the database for {string}")
    public void thereShouldBeNoAppointmentsFor(String name) {
        List<Appointment> all = appointmentRepository.findAll();
        boolean hasOrphan = all.stream()
                .anyMatch(a -> a.getPatient() != null &&
                        name.equals(a.getPatient().getName()));
        assertThat(hasOrphan).isFalse();
    }
}