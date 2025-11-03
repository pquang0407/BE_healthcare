package com.nckh.yte.repository;

import com.nckh.yte.entity.Patient;
import com.nckh.yte.entity.User; // ✅ Thêm import
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID; // ✅ Thêm import

// ✅ Sửa: Long -> UUID
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    // ✅ Thêm: Phương thức mới để AppointmentService sử dụng
    Optional<Patient> findByUser(User user);
    
    // (Bạn có thể xóa phương thức cũ này đi nếu không dùng ở đâu khác)
    Optional<Patient> findByUser_Username(String username); 
}