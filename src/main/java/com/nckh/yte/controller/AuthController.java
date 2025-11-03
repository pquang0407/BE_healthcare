package com.nckh.yte.controller;

import com.nckh.yte.dto.*;
import com.nckh.yte.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService service;

   @PostMapping("/register")
   public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
            var id = service.register(req);
        return ResponseEntity.ok(new ApiResponse("Registered successfully", id));
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        // On successful authentication return the AuthResponse directly.  The
        // front–end expects the token, fullName and role fields at the top
        // level of the JSON response rather than nested under a "data" key.
        return ResponseEntity.ok(service.login(req));
    }
}
