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
import com.medicalapp.repository.PatientRepository;

@Controller
@RequestMapping("/patients")
public class PatientController {
    
    @Autowired
    private PatientRepository patientRepository;
    
    @GetMapping
    public String listPatients(Model model) {
        model.addAttribute("patients", patientRepository.findAll());
        return "patients";
    }
    
    @GetMapping("/new")
    public String showPatientForm(Model model) {
        model.addAttribute("patient", new Patient());
        return "patient-form";
    }
    
    @PostMapping
    public String savePatient(@ModelAttribute Patient patient) {
        patientRepository.save(patient);
        return "redirect:/patients";
    }

    @GetMapping("/delete/{id}")
    public String deletePatient(@PathVariable Long id) {
        patientRepository.deleteById(id);
        return "redirect:/patients";
    }
}