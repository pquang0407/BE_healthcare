package com.nckh.yte.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate; // ✅ Import
import java.time.LocalDateTime;
import java.util.UUID; 

@Entity
@Table(name = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID) 
    private UUID id; 

    @ManyToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE}) // ✅ Thêm cascade
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "nurse_id")
    private Nurse nurse;

    // Các trường này là thời gian KHÁM CHÍNH THỨC (do BE xếp)
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Column(length = 500)
    private String symptom; // ✅ Đổi "reason" thành "symptom" cho khớp FE

    // === Các trường lưu YÊU CẦU GỐC từ FE ===
    private LocalDate preferredDate;
    private String preferredWindow;
    // ========================================

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.PENDING; // ✅ Đổi mặc định thành PENDING
}