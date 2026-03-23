package com.medicalapp.config;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hl7.fhir.r4.model.Patient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

@Configuration
public class FhirDataStore {

    private final List<Patient> patients = new ArrayList<>();

    /**
     * Shared FHIR R4 context bean — used by both FhirDataStore and FhirImportService.
     */
    @Bean
    public FhirContext fhirContext() {
        return FhirContext.forR4();
    }

    /**
     * Loads fhir-sample-patients.json from src/main/resources on startup,
     * parses each entry and stores them as FHIR R4 Patient objects in memory.
     */
    @Bean
    public List<Patient> fhirPatients(FhirContext fhirContext) {
        try {
            ClassPathResource resource = new ClassPathResource("fhir-sample-patients.json");
            try (InputStream is = resource.getInputStream()) {
                String bundleJson = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                ObjectMapper mapper = new ObjectMapper();
                JsonNode bundle = mapper.readTree(bundleJson);
                JsonNode entries = bundle.get("entry");

                IParser parser = fhirContext.newJsonParser();

                if (entries != null && entries.isArray()) {
                    for (JsonNode entry : entries) {
                        JsonNode resourceNode = entry.get("resource");
                        if (resourceNode != null) {
                            String resourceJson = resourceNode.toString();
                            Patient patient = parser.parseResource(Patient.class, resourceJson);
                            patients.add(patient);
                        }
                    }
                }
                System.out.println("✅ FHIR data store loaded " + patients.size() + " patients from fhir-sample-patients.json");
            }
        } catch (Exception e) {
            System.out.println("⚠️  Could not load FHIR sample data: " + e.getMessage());
        }
        return Collections.unmodifiableList(patients);
    }
}