package com.nckh.yte.controller;

import com.nckh.yte.repository.UserRepository;
import com.nckh.yte.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository repo;

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        var me = (UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(repo.findById(me.getId()));
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<?> profile(@PathVariable UUID id, Authentication auth) {
        var me = (UserDetailsImpl) auth.getPrincipal();
        if (!me.getId().equals(id) && !me.hasRole("ADMIN"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("❌ Không có quyền");
        return ResponseEntity.ok(repo.findById(id));
    }
}
