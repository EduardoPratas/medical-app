// PatientRecordDTO.java
package com.medicalapp.dto;

import java.util.List;

import com.medicalapp.model.Appointment;
import com.medicalapp.model.MedicalNote;
import com.medicalapp.model.Patient;
import com.medicalapp.model.TestResult;

public class PatientRecordDTO {
    private Patient patient;
    private List<Appointment> appointments;
    private List<MedicalNote> medicalNotes;
    private List<TestResult> testResults;
    
    // Constructor
    public PatientRecordDTO(Patient patient, List<Appointment> appointments, 
                            List<MedicalNote> medicalNotes, List<TestResult> testResults) {
        this.patient = patient;
        this.appointments = appointments;
        this.medicalNotes = medicalNotes;
        this.testResults = testResults;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    public List<MedicalNote> getMedicalNotes() {
        return medicalNotes;
    }

    public void setMedicalNotes(List<MedicalNote> medicalNotes) {
        this.medicalNotes = medicalNotes;
    }

    public List<TestResult> getTestResults() {
        return testResults;
    }

    public void setTestResults(List<TestResult> testResults) {
        this.testResults = testResults;
    }

}