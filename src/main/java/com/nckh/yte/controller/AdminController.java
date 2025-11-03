package com.nckh.yte.controller;

import com.nckh.yte.entity.User;
import com.nckh.yte.repository.UserRepository;
import com.nckh.yte.repository.DoctorRepository;
import com.nckh.yte.repository.NurseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
    import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DoctorRepository doctorRepository;
    private final NurseRepository nurseRepository;

    // =========================
    // HEALTH CHECK
    // =========================
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "time", Instant.now().toString()
        ));
    }

    // =========================
    // THÔNG TIN ADMIN HIỆN TẠI
    // =========================
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        String username = (auth != null ? auth.getName() : "anonymous");
        Set<String> roles = (auth == null) ? Set.of() :
                auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet());

        return ResponseEntity.ok(Map.of(
                "username", username,
                "roles", roles
        ));
    }

    // =========================
    // LẤY DANH SÁCH USER (TRỪ ADMIN)
    // =========================
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userRepository.findAll();

        List<Map<String, Object>> filtered = users.stream()
                // ⚙️ loại ADMIN
                .filter(u -> {
                    String roleName = Optional.ofNullable(u.getRole())
                            .map(r -> r.getName())
                            .orElse("");
                    return !roleName.equalsIgnoreCase("ROLE_ADMIN");
                })
                .map(u -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", u.getId().toString());
                    map.put("fullName", Optional.ofNullable(u.getFullName()).orElse(""));
                    map.put("username", Optional.ofNullable(u.getUsername()).orElse(""));
                    map.put("password", "hidden");

                    String role = Optional.ofNullable(u.getRole())
                            .map(r -> r.getName())
                            .orElse("");
                    if (role.startsWith("ROLE_")) {
                        role = role.substring(5);
                    }
                    map.put("role", role);

                    map.put("enabled", u.isEnabled());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(filtered);
    }

    // =========================
    // RESET PASSWORD (UUID)
    // =========================
    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<?> resetPassword(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body
    ) {
        String newPass = body.getOrDefault("newPassword", "").trim();
        if (newPass.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu mới không được để trống"));
        }

        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Không tìm thấy người dùng"));
        }

        User u = opt.get();
        u.setPassword(passwordEncoder.encode(newPass));
        userRepository.save(u);

        return ResponseEntity.ok(Map.of(
                "message", "✅ Đổi mật khẩu thành công cho " + u.getUsername(),
                "changedAt", Instant.now().toString()
        ));
    }

    // =========================
    // XOÁ TÀI KHOẢN (UUID)
    // =========================
    /**
     * Xoá tài khoản người dùng dựa trên UUID. Đồng thời,
     * nếu user thuộc vai trò DOCTOR hoặc NURSE thì sẽ duyệt qua
     * danh sách bác sĩ hoặc y tá và xoá bản ghi có username trùng khớp.
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "Không tìm thấy người dùng để xoá"
            ));
        }

        User user = opt.get();
        String username = Optional.ofNullable(user.getUsername()).orElse("");
        // Xác định role (bỏ prefix ROLE_ nếu có)
        String roleName = Optional.ofNullable(user.getRole())
                .map(r -> r.getName())
                .orElse("");
        if (roleName.startsWith("ROLE_")) {
            roleName = roleName.substring(5);
        }

        // Nếu là bác sĩ hoặc y tá, xoá hồ sơ tương ứng bằng cách duyệt findAll()
        try {
            if (roleName.equalsIgnoreCase("DOCTOR")) {
                doctorRepository.findAll().stream()
                    .filter(d -> username.equals(d.getUsername()))
                    .forEach(doctorRepository::delete);
            } else if (roleName.equalsIgnoreCase("NURSE")) {
                nurseRepository.findAll().stream()
                    .filter(n -> username.equals(n.getUsername()))
                    .forEach(nurseRepository::delete);
            }
        } catch (Exception e) {
            System.err.println("[WARN] Lỗi khi xoá hồ sơ phụ: " + e.getMessage());
        }

        // Xoá user
        userRepository.deleteById(id);

        return ResponseEntity.ok(Map.of(
                "message", "✅ Xoá tài khoản thành công",
                "deletedId", id.toString(),
                "username", username
        ));
    }

}
