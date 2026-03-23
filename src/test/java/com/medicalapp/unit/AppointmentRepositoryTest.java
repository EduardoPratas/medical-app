package com.medicalapp.unit;

import com.medicalapp.model.Appointment;
import com.medicalapp.model.Patient;
import com.medicalapp.repository.AppointmentRepository;
import com.medicalapp.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AppointmentRepository.
 *
 * Uses @DataJpaTest which loads only the JPA slice of the Spring context
 * (repositories + H2) — no controllers, no services, no web layer.
 * This is the PKB standard for testing repository query methods.
 */
@DataJpaTest
class AppointmentRepositoryTest {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setName("Oliver Hartley");
        patient.setEmail("oliver.hartley@nhs.net");
        patient.setPhone("07700900001");
        patientRepository.save(patient);
    }

    @Test
    @DisplayName("Should save an appointment and retrieve it for the correct patient")
    void shouldSaveAndRetrieveAppointment() {
        // Given
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctorName("Dr. Smith");
        appointment.setReason("Annual checkup");
        appointment.setStatus("SCHEDULED");
        appointment.setAppointmentDate(LocalDateTime.of(2026, 6, 15, 10, 0));

        // When
        appointmentRepository.save(appointment);
        List<Appointment> results = appointmentRepository
                .findByPatientOrderByAppointmentDateDesc(patient);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getDoctorName()).isEqualTo("Dr. Smith");
        assertThat(results.get(0).getReason()).isEqualTo("Annual checkup");
        assertThat(results.get(0).getStatus()).isEqualTo("SCHEDULED");
    }

    @Test
    @DisplayName("Should return appointments ordered by date descending")
    void shouldReturnAppointmentsOrderedByDateDescending() {
        // Given - two appointments on different dates
        Appointment older = new Appointment();
        older.setPatient(patient);
        older.setDoctorName("Dr. Jones");
        older.setReason("Follow-up");
        older.setStatus("COMPLETED");
        older.setAppointmentDate(LocalDateTime.of(2025, 1, 10, 9, 0));

        Appointment newer = new Appointment();
        newer.setPatient(patient);
        newer.setDoctorName("Dr. Smith");
        newer.setReason("Checkup");
        newer.setStatus("SCHEDULED");
        newer.setAppointmentDate(LocalDateTime.of(2026, 6, 15, 10, 0));

        appointmentRepository.save(older);
        appointmentRepository.save(newer);

        // When
        List<Appointment> results = appointmentRepository
                .findByPatientOrderByAppointmentDateDesc(patient);

        // Then - newest first
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getDoctorName()).isEqualTo("Dr. Smith");
        assertThat(results.get(1).getDoctorName()).isEqualTo("Dr. Jones");
    }

    @Test
    @DisplayName("Should return empty list when patient has no appointments")
    void shouldReturnEmptyListForPatientWithNoAppointments() {
        // When
        List<Appointment> results = appointmentRepository
                .findByPatientOrderByAppointmentDateDesc(patient);

        // Then
        assertThat(results).isEmpty();
    }
}