package com.medicalapp.integration;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.medicalapp.model.Patient;
import com.medicalapp.repository.PatientRepository;

/**
 * Integration tests for PatientController.
 *
 * Tests the full request/response cycle for patient deletion,
 * verifying both the HTTP response and the database state.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatientRepository patientRepository;

    private Patient patient;

    @BeforeEach
    void setUp() {
        patientRepository.deleteAll();

        patient = new Patient();
        patient.setName("Arthur Blackwood");
        patient.setEmail("arthur.blackwood@nhs.net");
        patient.setPhone("07700900005");
        patientRepository.save(patient);
    }

    @Test
    @DisplayName("GET /patients should return the patient list page")
    void shouldReturnPatientListPage() throws Exception {
        mockMvc.perform(get("/patients"))
                .andExpect(status().isOk())
                .andExpect(view().name("patients"))
                .andExpect(model().attributeExists("patients"));
    }

    @Test
    @DisplayName("GET /patients/delete/{id} should delete the patient and redirect to patient list")
    void shouldDeletePatientAndRedirect() throws Exception {
        Long id = patient.getId();

        mockMvc.perform(get("/patients/delete/" + id))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patients"));

        assertThat(patientRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("GET /patients/delete/{id} should only delete the specified patient")
    void shouldOnlyDeleteSpecifiedPatient() throws Exception {
        // Given - a second patient
        Patient other = new Patient();
        other.setName("Evelyn Marsden");
        other.setEmail("evelyn.marsden@nhs.net");
        patientRepository.save(other);

        // When - delete only the first patient
        mockMvc.perform(get("/patients/delete/" + patient.getId()))
                .andExpect(status().is3xxRedirection());

        // Then - second patient still exists
        assertThat(patientRepository.findById(other.getId())).isPresent();
        assertThat(patientRepository.count()).isEqualTo(1);
    }
}