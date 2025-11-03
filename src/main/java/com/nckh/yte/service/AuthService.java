package com.nckh.yte.service;

import com.nckh.yte.dto.*;
import com.nckh.yte.entity.Role;
import com.nckh.yte.entity.User;
import com.nckh.yte.repository.RoleRepository;
import com.nckh.yte.repository.UserRepository;
import com.nckh.yte.security.JwtUtil;
import com.nckh.yte.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final JwtUtil jwtUtil;

    // =============================
    // ĐĂNG NHẬP
    // =============================
    public AuthResponse login(LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));

        UserDetailsImpl principal = (UserDetailsImpl) auth.getPrincipal();

        // Lấy set vai trò từ GrantedAuthority -> bỏ "ROLE_"
        Set<String> roles = principal.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", "")) // giữ dạng ADMIN, DOCTOR, ...
                .collect(Collectors.toSet());

        // Sinh JWT: (username, toàn bộ roles, userId)
        String token = jwtUtil.generateToken(principal.getUsername(), roles, principal.getId());

        // Tên hiển thị (ưu tiên fullName)
        String fullName = (principal.getFullName() != null && !principal.getFullName().isBlank())
                ? principal.getFullName()
                : principal.getUsername();

        String primaryRole = roles.stream().findFirst().orElse("");

        return new AuthResponse(token, fullName, primaryRole);
    }

    // =============================
    // ĐĂNG KÝ — MẶC ĐỊNH PATIENT
    // =============================
    public UUID register(RegisterRequest req) {
        if (userRepo.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        // ⚙️ DB đang lưu role là "PATIENT" (không có prefix)
        Role patient = roleRepo.findByName("PATIENT")
                .orElseThrow(() -> new IllegalStateException("Thiếu role PATIENT — hãy seed roles trước"));

        User u = new User();
        u.setUsername(req.getUsername());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setFullName(req.getFullName());
        u.setEnabled(true);

        // 1 role duy nhất
        u.setRole(patient);

        u = userRepo.save(u);
        return u.getId();
    }
}
