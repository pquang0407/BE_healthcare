package com.nckh.yte.service;

import com.nckh.yte.entity.Nurse;
import com.nckh.yte.entity.Role;
import com.nckh.yte.entity.User;
import com.nckh.yte.repository.NurseRepository;
import com.nckh.yte.repository.RoleRepository;
import com.nckh.yte.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NurseService {

    private final NurseRepository nurseRepo;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;

    @Transactional
    public Nurse create(String fullName, String username, String password) {
        // Kiểm tra username trùng
        if (userRepo.existsByUsername(username)) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }

        // Mã hóa mật khẩu
        String encoded = encoder.encode(password);

        // 1️⃣ Lưu hồ sơ y tá
        Nurse nurse = Nurse.builder()
                .fullName(fullName)
                .username(username)
                .password(encoded)
                .build();
        nurseRepo.save(nurse);

        // 2️⃣ Lấy role "NURSE"
        Role roleNurse = roleRepo.findByName("NURSE")
                .orElseThrow(() -> new RuntimeException("Thiếu role NURSE trong bảng roles"));

        // 3️⃣ Tạo user login — gán trực tiếp role
        User user = User.builder()
                .username(username)
                .password(encoded)
                .fullName(fullName)
                .enabled(true)
                .role(roleNurse) // ✅ gán trực tiếp
                .build();

        userRepo.save(user);

        return nurse;
    }
}
