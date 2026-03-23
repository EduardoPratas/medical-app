package com.medicalapp.integration;

import static org.assertj.core.api.Assertions.assertThat;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

/**
 * Integration tests for the embedded FHIR endpoint.
 *
 * Loads the full Spring context and verifies that GET /fhir/Patient
 * returns a valid FHIR R4 Bundle containing the expected sample patients.
 * This mirrors how a real FHIR client (e.g. NHS App) would call PKB's API.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FhirEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /fhir/Patient should return HTTP 200 with application/json content type")
    void shouldReturnOkWithJsonContentType() throws Exception {
        mockMvc.perform(get("/fhir/Patient"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    @DisplayName("GET /fhir/Patient should return a valid FHIR R4 Bundle")
    void shouldReturnValidFhirBundle() throws Exception {
        MvcResult result = mockMvc.perform(get("/fhir/Patient"))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        // Parse with HAPI FHIR — will throw if not valid FHIR
        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();
        Bundle bundle = parser.parseResource(Bundle.class, responseBody);

        assertThat(bundle).isNotNull();
        assertThat(bundle.getType()).isEqualTo(Bundle.BundleType.SEARCHSET);
    }

    @Test
    @DisplayName("GET /fhir/Patient should return 5 sample patients from fhir-sample-patients.json")
    void shouldReturnFiveSamplePatients() throws Exception {
        MvcResult result = mockMvc.perform(get("/fhir/Patient"))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        FhirContext ctx = FhirContext.forR4();
        Bundle bundle = ctx.newJsonParser().parseResource(Bundle.class, responseBody);

        assertThat(bundle.getEntry()).hasSize(5);
        assertThat(bundle.getTotal()).isEqualTo(5);
    }

    @Test
    @DisplayName("GET /fhir/Patient should include a patient named Oliver Hartley")
    void shouldIncludeOliverHartley() throws Exception {
        MvcResult result = mockMvc.perform(get("/fhir/Patient"))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        FhirContext ctx = FhirContext.forR4();
        Bundle bundle = ctx.newJsonParser().parseResource(Bundle.class, responseBody);

        boolean found = bundle.getEntry().stream()
                .map(e -> (org.hl7.fhir.r4.model.Patient) e.getResource())
                .anyMatch(p -> p.getName().stream()
                        .anyMatch(n -> "Oliver Hartley".equals(n.getText())));

        assertThat(found).isTrue();
    }
}