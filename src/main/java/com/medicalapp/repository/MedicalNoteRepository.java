package com.medicalapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.medicalapp.model.MedicalNote;
import com.medicalapp.model.Patient;

public interface MedicalNoteRepository extends JpaRepository<MedicalNote, Long> {
    List<MedicalNote> findByPatientOrderByCreatedAtDesc(Patient patient);
}