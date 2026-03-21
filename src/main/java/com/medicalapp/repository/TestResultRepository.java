package com.medicalapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.medicalapp.model.Patient;
import com.medicalapp.model.TestResult;

public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    List<TestResult> findByPatientOrderByTestDateDesc(Patient patient);
}