package com.nckh.yte.entity;

import com.fasterxml.jackson.annotation.JsonBackReference; // ✅ Thêm import
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String fullName;

    @Builder.Default
    private boolean enabled = true;

    // ⚡️ Mỗi user chỉ có 1 role
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    // 🔗 Mỗi user có thể có 1 bệnh nhân tương ứng (nếu là role PATIENT)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonBackReference // ✅ Thêm dòng này
    private Patient patient;
}