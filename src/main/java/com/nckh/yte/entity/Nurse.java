package com.nckh.yte.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "nurse")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Nurse {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String fullName;

    @Column(unique = true)
    private String username;

    private String password;
    
    @Column(length = 100)
    private String department; // ✅ KHOA CÔNG TÁC
}