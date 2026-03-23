package com.medicalapp.unit;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.medicalapp.model.Patient;
import com.medicalapp.repository.PatientRepository;
import com.medicalapp.service.FhirImportService;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FhirImportService.
 *
 * Tests the mapping logic from FHIR Patient resources to the local Patient model
 * in isolation — no Spring context, no database, no HTTP calls.
 * External dependencies (PatientRepository, RestTemplate) are mocked.
 */
@ExtendWith(MockitoExtension.class)
class FhirImportServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FhirImportService fhirImportService;

    private FhirContext fhirContext;
    private IParser parser;

    @BeforeEach
    void setUp() {
        fhirContext = FhirContext.forR4();
        parser = fhirContext.newJsonParser();
        ReflectionTestUtils.setField(fhirImportService, "fhirContext", fhirContext);
        ReflectionTestUtils.setField(fhirImportService, "fhirServerUrl", "http://localhost:8080/fhir");
        ReflectionTestUtils.setField(fhirImportService, "restTemplate", restTemplate);
    }

    @Test
    @DisplayName("Should import a new patient when they do not exist in the database")
    void shouldImportNewPatient() {
        // Given - a FHIR Bundle with one patient
        String bundleJson = buildFhirBundle("Oliver Hartley", "oliver.hartley@nhs.net", "07700900001", "1978-04-12");
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(bundleJson);
        when(patientRepository.findByEmail("oliver.hartley@nhs.net")).thenReturn(Optional.empty());

        // When
        FhirImportService.FhirImportResult result = fhirImportService.importPatients();

        // Then
        assertThat(result.getImported()).isEqualTo(1);
        assertThat(result.getSkipped()).isEqualTo(0);

        ArgumentCaptor<Patient> captor = ArgumentCaptor.forClass(Patient.class);
        verify(patientRepository).save(captor.capture());
        Patient saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("Oliver Hartley");
        assertThat(saved.getEmail()).isEqualTo("oliver.hartley@nhs.net");
        assertThat(saved.getPhone()).isEqualTo("07700900001");
    }

    @Test
    @DisplayName("Should skip a patient who already exists in the database (matched by email)")
    void shouldSkipExistingPatient() {
        // Given - patient already in H2
        String bundleJson = buildFhirBundle("Oliver Hartley", "oliver.hartley@nhs.net", "07700900001", "1978-04-12");
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(bundleJson);
        when(patientRepository.findByEmail("oliver.hartley@nhs.net")).thenReturn(Optional.of(new Patient()));

        // When
        FhirImportService.FhirImportResult result = fhirImportService.importPatients();

        // Then
        assertThat(result.getImported()).isEqualTo(0);
        assertThat(result.getSkipped()).isEqualTo(1);
        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should skip a FHIR patient who has no email address")
    void shouldSkipPatientWithNoEmail() {
        // Given - FHIR patient with no email telecom
        String bundleJson = buildFhirBundle("George Whitfield", null, "07700900003", "1962-01-27");
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(bundleJson);

        // When
        FhirImportService.FhirImportResult result = fhirImportService.importPatients();

        // Then
        assertThat(result.getSkipped()).isEqualTo(1);
        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should report failure gracefully when FHIR server is unreachable")
    void shouldHandleFhirServerConnectionFailure() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        // When
        FhirImportService.FhirImportResult result = fhirImportService.importPatients();

        // Then
        assertThat(result.getImported()).isEqualTo(0);
        assertThat(result.getMessages()).anyMatch(m -> m.contains("Failed to connect"));
    }

    // --- Helper ---

    private String buildFhirBundle(String name, String email, String phone, String birthDate) {
        org.hl7.fhir.r4.model.Patient fhirPatient = new org.hl7.fhir.r4.model.Patient();
        fhirPatient.addName().setText(name);
        if (email != null) {
            fhirPatient.addTelecom()
                    .setSystem(ContactPoint.ContactPointSystem.EMAIL)
                    .setValue(email);
        }
        if (phone != null) {
            fhirPatient.addTelecom()
                    .setSystem(ContactPoint.ContactPointSystem.PHONE)
                    .setValue(phone);
        }
        if (birthDate != null) {
            try {
                fhirPatient.setBirthDateElement(new DateType(birthDate));
            } catch (Exception ignored) {}
        }

        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.SEARCHSET);
        bundle.addEntry().setResource(fhirPatient);

        return parser.encodeResourceToString(bundle);
    }
}