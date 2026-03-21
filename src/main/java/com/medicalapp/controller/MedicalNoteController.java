package com.medicalapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.medicalapp.model.MedicalNote;
import com.medicalapp.model.Patient;
import com.medicalapp.repository.MedicalNoteRepository;
import com.medicalapp.repository.PatientRepository;

@Controller
@RequestMapping("/patients/{patientId}/notes")
public class MedicalNoteController {

    @Autowired
    private MedicalNoteRepository medicalNoteRepository;

    @Autowired
    private PatientRepository patientRepository;

    @GetMapping("/new")
    public String showNoteForm(@PathVariable Long patientId, Model model) {
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new RuntimeException("Patient not found"));
        model.addAttribute("patient", patient);
        model.addAttribute("note", new MedicalNote());
        return "medicalnote-form";
    }

    @PostMapping
    public String saveNote(@PathVariable Long patientId,
                           @ModelAttribute MedicalNote note) {
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new RuntimeException("Patient not found"));
        note.setPatient(patient);
        medicalNoteRepository.save(note);
        return "redirect:/patients/" + patientId + "/record";
    }
}