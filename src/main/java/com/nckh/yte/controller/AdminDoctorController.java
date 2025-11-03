package com.nckh.yte.controller;

import com.nckh.yte.entity.Doctor;
import com.nckh.yte.service.DoctorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public record AdminDoctorController(DoctorService service) {

    record Req(String fullName, String username, String password) {}

    @PostMapping("/create-doctor")
    public ResponseEntity<?> createDoctor(@RequestBody Req req) {
        try {
            Doctor doctor = service.create(req.fullName(), req.username(), req.password());
            return ResponseEntity.ok(Map.of(
                    "message", "Tạo bác sĩ thành công!",
                    "id", doctor.getId(),
                    "fullName", doctor.getFullName(),
                    "username", doctor.getUsername()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(409).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Lỗi server khi tạo bác sĩ."));
        }
    }
}
