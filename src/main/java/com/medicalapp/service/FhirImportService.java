package com.medicalapp.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.medicalapp.repository.PatientRepository;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

@Service
public class FhirImportService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private FhirContext fhirContext;

    @Value("${fhir.server.url:http://localhost:8080/fhir}")
    private String fhirServerUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public FhirImportResult importPatients() {
        int imported = 0;
        int skipped = 0;
        List<String> messages = new ArrayList<>();

        try {
            // Call our embedded /fhir/Patient endpoint
            String url = fhirServerUrl + "/Patient";
            String response = restTemplate.getForObject(url, String.class);

            // Parse the FHIR Bundle using HAPI
            IParser parser = fhirContext.newJsonParser();
            Bundle bundle = parser.parseResource(Bundle.class, response);

            if (bundle.getEntry().isEmpty()) {
                messages.add("No patients found on FHIR server.");
                return new FhirImportResult(imported, skipped, messages);
            }

            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                if (!(entry.getResource() instanceof Patient fhirPatient)) continue;

                try {
                    String name = extractName(fhirPatient);
                    String email = extractTelecom(fhirPatient, ContactPoint.ContactPointSystem.EMAIL);

                    if (email == null || email.isBlank()) {
                        messages.add("Skipped '" + name + "' — no email in FHIR record.");
                        skipped++;
                        continue;
                    }

                    // Skip if already exists in H2
                    if (patientRepository.findByEmail(email).isPresent()) {
                        messages.add("Skipped '" + name + "' — already exists.");
                        skipped++;
                        continue;
                    }

                    String phone = extractTelecom(fhirPatient, ContactPoint.ContactPointSystem.PHONE);
                    LocalDate dateOfBirth = extractBirthDate(fhirPatient);

                    com.medicalapp.model.Patient patient = new com.medicalapp.model.Patient();
                    patient.setName(name);
                    patient.setEmail(email);
                    patient.setPhone(phone);
                    patient.setDateOfBirth(dateOfBirth);
                    patientRepository.save(patient);

                    messages.add("Imported: " + name);
                    imported++;

                } catch (Exception e) {
                    messages.add("Error processing a patient: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            messages.add("Failed to connect to FHIR server at " + fhirServerUrl + ": " + e.getMessage());
        }

        return new FhirImportResult(imported, skipped, messages);
    }

    // --- Helpers ---

    private String extractName(Patient fhirPatient) {
        if (!fhirPatient.getName().isEmpty()) {
            var name = fhirPatient.getName().get(0);
            if (name.hasText()) return name.getText();
            String given = name.getGiven().isEmpty() ? "" : name.getGiven().get(0).getValue();
            String family = name.hasFamily() ? name.getFamily() : "";
            return (given + " " + family).trim();
        }
        return "Unknown";
    }

    private String extractTelecom(Patient fhirPatient, ContactPoint.ContactPointSystem system) {
        return fhirPatient.getTelecom().stream()
                .filter(t -> t.getSystem() == system)
                .map(ContactPoint::getValue)
                .findFirst()
                .orElse(null);
    }

    private LocalDate extractBirthDate(Patient fhirPatient) {
        if (fhirPatient.hasBirthDate()) {
            return fhirPatient.getBirthDate()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }
        return null;
    }

    // --- Result DTO ---

    public static class FhirImportResult {
        private final int imported;
        private final int skipped;
        private final List<String> messages;

        public FhirImportResult(int imported, int skipped, List<String> messages) {
            this.imported = imported;
            this.skipped = skipped;
            this.messages = messages;
        }

        public int getImported() { return imported; }
        public int getSkipped() { return skipped; }
        public List<String> getMessages() { return messages; }
    }
}