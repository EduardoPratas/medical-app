package com.medicalapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.medicalapp.service.FhirImportService;
import com.medicalapp.service.FhirImportService.FhirImportResult;

@Controller
@RequestMapping("/fhir")
public class FhirImportController {

    @Autowired
    private FhirImportService fhirImportService;

    @GetMapping("/import")
    public String showImportPage() {
        return "fhir-import";
    }

    @PostMapping("/import")
    public String runImport(Model model) {
        FhirImportResult result = fhirImportService.importPatients();
        model.addAttribute("result", result);
        return "fhir-import";
    }
}