package com.nckh.yte.service;

import com.nckh.yte.entity.Doctor;
import com.nckh.yte.entity.Role;
import com.nckh.yte.entity.User;
import com.nckh.yte.repository.DoctorRepository;
import com.nckh.yte.repository.RoleRepository;
import com.nckh.yte.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepo;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;

    @Transactional
    public Doctor create(String fullName, String username, String password) {
        // Kiểm tra username trùng trong bảng users
        if (userRepo.existsByUsername(username)) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }

        // Mã hóa mật khẩu
        String encoded = encoder.encode(password);

        // 1️⃣ Lưu hồ sơ bác sĩ (bảng doctor)
        Doctor doctor = Doctor.builder()
                .fullName(fullName)
                .username(username)
                .password(encoded)
                .build();
        doctorRepo.save(doctor);

        // 2️⃣ Lấy role "DOCTOR" từ bảng roles
        Role roleDoctor = roleRepo.findByName("DOCTOR")
                .orElseThrow(() -> new RuntimeException("Thiếu role DOCTOR trong bảng roles"));

        // 3️⃣ Tạo user login — gán 1 role duy nhất
        User user = User.builder()
                .username(username)
                .password(encoded)
                .fullName(fullName)
                .enabled(true)
                .role(roleDoctor) // ✅ set trực tiếp, KHÔNG gọi add()
                .build();

        userRepo.save(user);

        return doctor;
    }
}
