package com.medicalapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.medicalapp.model.Appointment;
import com.medicalapp.model.Patient;
import com.medicalapp.repository.AppointmentRepository;
import com.medicalapp.repository.PatientRepository;

@Controller
@RequestMapping("/patients/{patientId}/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @GetMapping("/new")
    public String showAppointmentForm(@PathVariable Long patientId, Model model) {
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new RuntimeException("Patient not found"));
        model.addAttribute("patient", patient);
        model.addAttribute("appointment", new Appointment());
        return "appointment-form";
    }

    @PostMapping
    public String saveAppointment(@PathVariable Long patientId,
                                  @ModelAttribute Appointment appointment) {
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new RuntimeException("Patient not found"));
        appointment.setPatient(patient);
        appointmentRepository.save(appointment);
        return "redirect:/patients/" + patientId + "/record";
    }
}