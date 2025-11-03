package com.nckh.yte.service;

import com.nckh.yte.entity.Patient;
import com.nckh.yte.entity.Role;
import com.nckh.yte.entity.User;
import com.nckh.yte.repository.PatientRepository;
import com.nckh.yte.repository.RoleRepository;
import com.nckh.yte.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * PatientService — now backed by UserRepository.
 * It returns users having role=PATIENT, keeping API unchanged for frontend.
 */
@Service
@RequiredArgsConstructor
public class PatientService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PatientRepository patientRepository; // optional, still used for compatibility

    /**
     * Return all users with role=PATIENT.
     */
    public List<User> getAll() {
        Role role = roleRepository.findByName("PATIENT")
                .orElseThrow(() -> new IllegalArgumentException("Role PATIENT not found"));
        return userRepository.findAll().stream()
                .filter(u -> u.getRole().getId().equals(role.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Get one patient (user) by id.
     */
    public User getById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!"PATIENT".equalsIgnoreCase(user.getRole().getName())) {
            throw new IllegalArgumentException("User is not a patient");
        }
        return user;
    }

    /**
     * Create a new user with role=PATIENT (acts like patient creation).
     */
    public User create(User input) {
        if (input.getUsername() == null || input.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username (email) is required");
        }

        if (userRepository.existsByUsername(input.getUsername())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Role role = roleRepository.findByName("PATIENT")
                .orElseThrow(() -> new RuntimeException("Role PATIENT not found"));
        input.setRole(role);
        input.setEnabled(true);
        return userRepository.save(input);
    }

    /**
     * Update patient info (fullName, username/email).
     */
    public User update(UUID id, User updated) {
        User existing = getById(id);
        existing.setFullName(updated.getFullName());
        existing.setUsername(updated.getUsername());
        return userRepository.save(existing);
    }

    /**
     * Delete patient (user with role PATIENT).
     */
    public void delete(UUID id) {
        User user = getById(id);
        userRepository.delete(user);
    }
}
