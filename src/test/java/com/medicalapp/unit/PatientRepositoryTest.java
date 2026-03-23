package com.medicalapp.unit;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.medicalapp.model.Patient;
import com.medicalapp.repository.PatientRepository;

/**
 * Unit tests for PatientRepository.
 *
 * Uses @DataJpaTest — only the JPA slice is loaded.
 * Tests the custom findByEmail query and basic delete behaviour.
 */
@DataJpaTest
class PatientRepositoryTest {

    @Autowired
    private PatientRepository patientRepository;

    @Test
    @DisplayName("Should find a patient by email address")
    void shouldFindPatientByEmail() {
        // Given
        Patient patient = new Patient();
        patient.setName("Charlotte Pemberton");
        patient.setEmail("charlotte.pemberton@nhs.net");
        patientRepository.save(patient);

        // When
        Optional<Patient> found = patientRepository.findByEmail("charlotte.pemberton@nhs.net");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Charlotte Pemberton");
    }

    @Test
    @DisplayName("Should return empty when no patient exists with given email")
    void shouldReturnEmptyForUnknownEmail() {
        // When
        Optional<Patient> found = patientRepository.findByEmail("nobody@nhs.net");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should delete a patient by ID and no longer find them")
    void shouldDeletePatientById() {
        // Given
        Patient patient = new Patient();
        patient.setName("Arthur Blackwood");
        patient.setEmail("arthur.blackwood@nhs.net");
        patientRepository.save(patient);
        Long id = patient.getId();

        // When
        patientRepository.deleteById(id);

        // Then
        assertThat(patientRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("Should enforce unique email constraint")
    void shouldEnforceUniqueEmailConstraint() {
        // Given
        Patient first = new Patient();
        first.setName("Evelyn Marsden");
        first.setEmail("evelyn.marsden@nhs.net");
        patientRepository.save(first);

        Patient duplicate = new Patient();
        duplicate.setName("Evelyn Marsden Duplicate");
        duplicate.setEmail("evelyn.marsden@nhs.net");

        // Then
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
            patientRepository.saveAndFlush(duplicate);
        });
    }
}