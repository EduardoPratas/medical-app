package com.medicalapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.medicalapp.dto.PatientRecordDTO;
import com.medicalapp.service.PatientRecordService;

@Controller
@RequestMapping("/patients")
public class PatientRecordController {
    
    @Autowired
    private PatientRecordService patientRecordService;
    
    @GetMapping("/{id}/record")
    public String viewPatientRecord(@PathVariable Long id, Model model) {
        PatientRecordDTO record = patientRecordService.getPatientRecord(id);
        model.addAttribute("record", record);
        return "patient-record";
    }
}