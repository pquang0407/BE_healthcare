package com.nckh.yte.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID; 

@Entity
@Table(name = "patient") // Tên table nên là "patients" (số nhiều)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) 
    private UUID id; 

    private String firstName;
    private String lastName;
    private LocalDate dob;
    private String phone;
    private String email;
    private String address;
    
    private String gender; // ✅ THÊM TRƯỜNG GIỚI TÍNH TỪ FE

    @Column(length = 100)
    private String department; 

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonManagedReference 
    private User user;

    public String getFullName() {
        String fn = firstName != null ? firstName.trim() : "";
        String ln = lastName != null ? lastName.trim() : "";
        return (fn + " " + ln).trim();
    }
}