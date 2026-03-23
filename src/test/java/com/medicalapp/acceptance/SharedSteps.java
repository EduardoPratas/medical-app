package com.medicalapp.acceptance;

import org.springframework.beans.factory.annotation.Autowired;

import com.medicalapp.model.Patient;
import com.medicalapp.repository.AppointmentRepository;
import com.medicalapp.repository.PatientRepository;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;

/**
 * Shared step definitions used across multiple feature files.
 * Cucumber loads all step definitions globally — each step text
 * must be defined exactly once across ALL step classes.
 */
public class SharedSteps {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Before
    public void cleanDatabase() {
        appointmentRepository.deleteAll();
        patientRepository.deleteAll();
    }

    @Given("a patient named {string} exists in the system")
    public void aPatientNamedExistsInTheSystem(String name) {
        String email = toEmail(name);
        if (patientRepository.findByEmail(email).isEmpty()) {
            Patient p = new Patient();
            p.setName(name);
            p.setEmail(email);
            p.setPhone("07700900001");
            patientRepository.save(p);
        }
    }

    public static String toEmail(String name) {
        return name.toLowerCase().replace(" ", ".") + "@nhs.net";
    }
}