package com.nckh.yte.repository;

import com.nckh.yte.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID; // ✅ Thêm import

// ✅ Sửa: Long -> UUID
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    // ✅ Sửa: Long -> UUID
    List<Appointment> findByDoctorId(UUID doctorId);

    // ✅ Sửa: Long -> UUID
    List<Appointment> findByNurseId(UUID nurseId);

    // ✅ Sửa: Long -> UUID
    List<Appointment> findByPatientId(UUID patientId);
}