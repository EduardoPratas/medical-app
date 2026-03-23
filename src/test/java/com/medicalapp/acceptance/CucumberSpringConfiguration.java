package com.medicalapp.acceptance;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import io.cucumber.spring.CucumberContextConfiguration;

/**
 * Shared Spring context configuration for all Cucumber step definitions.
 *
 * RANDOM_PORT starts a real embedded server so FhirImportService can call
 * /fhir/Patient via RestTemplate during acceptance tests.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CucumberSpringConfiguration {
}