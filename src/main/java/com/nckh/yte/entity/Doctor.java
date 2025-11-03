package com.nckh.yte.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "doctor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String fullName;

    @Column(unique = true)
    private String username;

    private String password;

    @Column(length = 100)
    private String specialty; // Chuyên khoa (ví dụ: Tim mạch)
    
    @Column(length = 100)
    private String department; // ✅ KHOA CÔNG TÁC (ví dụ: Khoa Nội)
}