package com.medicalapp.controller;

import java.util.List;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

@RestController
@RequestMapping("/fhir")
public class FhirPatientController {

    @Autowired
    private FhirContext fhirContext;

    @Autowired
    private List<Patient> fhirPatients;

    /**
     * GET /fhir/Patient
     * Returns all patients as a valid FHIR R4 Bundle in JSON format.
     * This simulates what a real FHIR server (e.g. NHS) would return.
     */
    @GetMapping(value = "/Patient", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getPatients() {
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.SEARCHSET);
        bundle.setTotal(fhirPatients.size());

        for (Patient patient : fhirPatients) {
            bundle.addEntry().setResource(patient);
        }

        IParser parser = fhirContext.newJsonParser().setPrettyPrint(true);
        return parser.encodeResourceToString(bundle);
    }
}