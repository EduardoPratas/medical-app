package com.medicalapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.medicalapp.dto.PatientRecordDTO;
import com.medicalapp.model.Patient;
import com.medicalapp.repository.AppointmentRepository;
import com.medicalapp.repository.MedicalNoteRepository;
import com.medicalapp.repository.PatientRepository;
import com.medicalapp.repository.TestResultRepository;

@Service
public class PatientRecordService {
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private MedicalNoteRepository medicalNoteRepository;
    
    @Autowired
    private TestResultRepository testResultRepository;
    
    public PatientRecordDTO getPatientRecord(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        return new PatientRecordDTO(
            patient,
            appointmentRepository.findByPatientOrderByAppointmentDateDesc(patient),
            medicalNoteRepository.findByPatientOrderByCreatedAtDesc(patient),
            testResultRepository.findByPatientOrderByTestDateDesc(patient)
        );
    }
}