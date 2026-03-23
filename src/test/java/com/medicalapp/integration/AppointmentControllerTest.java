package com.medicalapp.integration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.medicalapp.model.Appointment;
import com.medicalapp.model.Patient;
import com.medicalapp.repository.AppointmentRepository;
import com.medicalapp.repository.PatientRepository;

/**
 * Integration tests for AppointmentController.
 *
 * Loads the full Spring context and tests the full request/response cycle
 * including persistence to the H2 database.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private Patient patient;

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll();
        patientRepository.deleteAll();

        patient = new Patient();
        patient.setName("Oliver Hartley");
        patient.setEmail("oliver.hartley@nhs.net");
        patient.setPhone("07700900001");
        patientRepository.save(patient);
    }

    @Test
    @DisplayName("GET /patients/{id}/appointments/new should return the appointment form")
    void shouldDisplayAppointmentForm() throws Exception {
        mockMvc.perform(get("/patients/" + patient.getId() + "/appointments/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("appointment-form"))
                .andExpect(model().attributeExists("patient"))
                .andExpect(model().attributeExists("appointment"));
    }

    @Test
    @DisplayName("POST /patients/{id}/appointments should save the appointment and redirect to patient record")
    void shouldSaveAppointmentAndRedirect() throws Exception {
        mockMvc.perform(post("/patients/" + patient.getId() + "/appointments")
                        .param("doctorName", "Dr. Smith")
                        .param("reason", "Annual checkup")
                        .param("status", "SCHEDULED")
                        .param("appointmentDate", "2026-06-15T10:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patients/" + patient.getId() + "/record"));

        List<Appointment> saved = appointmentRepository.findByPatientOrderByAppointmentDateDesc(patient);
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getDoctorName()).isEqualTo("Dr. Smith");
        assertThat(saved.get(0).getReason()).isEqualTo("Annual checkup");
        assertThat(saved.get(0).getStatus()).isEqualTo("SCHEDULED");
    }

    @Test
    @DisplayName("POST /patients/{id}/appointments should persist appointment linked to correct patient")
    void shouldLinkAppointmentToCorrectPatient() throws Exception {
        mockMvc.perform(post("/patients/" + patient.getId() + "/appointments")
                        .param("doctorName", "Dr. Jones")
                        .param("reason", "Follow-up")
                        .param("status", "COMPLETED")
                        .param("appointmentDate", "2026-05-10T14:30"))
                .andExpect(status().is3xxRedirection());

        List<Appointment> saved = appointmentRepository.findByPatientOrderByAppointmentDateDesc(patient);
        assertThat(saved.get(0).getPatient().getId()).isEqualTo(patient.getId());
    }
}