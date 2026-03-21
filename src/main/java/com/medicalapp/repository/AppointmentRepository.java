package com.medicalapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.medicalapp.model.Appointment;
import com.medicalapp.model.Patient;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientOrderByAppointmentDateDesc(Patient patient);
}