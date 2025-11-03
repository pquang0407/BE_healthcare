package com.nckh.yte.config;

import com.nckh.yte.entity.Role;
import com.nckh.yte.entity.User;
import com.nckh.yte.repository.RoleRepository;
import com.nckh.yte.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    @Bean
    CommandLineRunner init(RoleRepository roleRepo,
                           UserRepository userRepo,
                           PasswordEncoder encoder) {
        return args -> {
            // 1) Đảm bảo các role tồn tại (DB đang dùng tên KHÔNG prefix ROLE_)
            roleRepo.findByName("ADMIN").orElseGet(() -> roleRepo.save(new Role(null, "ADMIN")));
            roleRepo.findByName("DOCTOR").orElseGet(() -> roleRepo.save(new Role(null, "DOCTOR")));
            roleRepo.findByName("NURSE").orElseGet(() -> roleRepo.save(new Role(null, "NURSE")));
            roleRepo.findByName("PATIENT").orElseGet(() -> roleRepo.save(new Role(null, "PATIENT")));

            // 2) Nếu chưa có bất kỳ user nào mang role ADMIN -> auto tạo/ nâng cấp
            boolean hasAnyAdmin = userRepo.findAll().stream()
                    .anyMatch(u -> u.getRole() != null && "ADMIN".equalsIgnoreCase(u.getRole().getName()));

            if (!hasAnyAdmin) {
                Role adminRole = roleRepo.findByName("ADMIN")
                        .orElseThrow(() -> new IllegalStateException("Missing role ADMIN"));

                // Nếu username 'admin' đã tồn tại nhưng chưa phải ADMIN -> nâng cấp
                var existingAdminUsername = userRepo.findByUsername("admin");
                if (existingAdminUsername.isPresent()) {
                    User u = existingAdminUsername.get();
                    u.setRole(adminRole);
                    if (u.getPassword() == null || u.getPassword().isBlank()) {
                        u.setPassword(encoder.encode("admin123")); // đổi sau khi đăng nhập
                    }
                    if (u.getFullName() == null || u.getFullName().isBlank()) {
                        u.setFullName("System Administrator");
                    }
                    u.setEnabled(true);
                    userRepo.save(u);
                } else {
                    // Tạo mới tài khoản admin mặc định
                    User u = new User();
                    u.setUsername("admin");
                    u.setPassword(encoder.encode("admin123")); // đổi sau khi đăng nhập
                    u.setFullName("System Administrator");
                    u.setEnabled(true);
                    u.setRole(adminRole); // 1 role duy nhất
                    userRepo.save(u);
                }
            }
        };
    }
}
