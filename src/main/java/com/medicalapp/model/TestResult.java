// TestResult.java - for lab results
package com.medicalapp.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "test_results")
public class TestResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;
    
    private String testName;
    private String result;
    private String referenceRange;
    private LocalDate testDate;
    
    public Long getId() {
        return id;
    }

    public Patient getPatient() {
        return patient;
    }

    public String getTestName() {
        return testName;
    }

    public String getResult() {
        return result;
    }

    public String getReferenceRange() {
        return referenceRange;
    }

    public LocalDate getTestDate() {
        return testDate;
    }
}