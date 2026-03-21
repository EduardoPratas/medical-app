package com.medicalapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.medicalapp.model.Patient;
import com.medicalapp.model.TestResult;
import com.medicalapp.repository.PatientRepository;
import com.medicalapp.repository.TestResultRepository;

@Controller
@RequestMapping("/patients/{patientId}/testresults")
public class TestResultController {

    @Autowired
    private TestResultRepository testResultRepository;

    @Autowired
    private PatientRepository patientRepository;

    @GetMapping("/new")
    public String showTestResultForm(@PathVariable Long patientId, Model model) {
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new RuntimeException("Patient not found"));
        model.addAttribute("patient", patient);
        model.addAttribute("testResult", new TestResult());
        return "testresult-form";
    }

    @PostMapping
    public String saveTestResult(@PathVariable Long patientId,
                                 @ModelAttribute TestResult testResult) {
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new RuntimeException("Patient not found"));
        testResult.setPatient(patient);
        testResultRepository.save(testResult);
        return "redirect:/patients/" + patientId + "/record";
    }
}