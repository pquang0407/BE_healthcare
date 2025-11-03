package com.nckh.yte.repository;

import com.nckh.yte.entity.Nurse;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NurseRepository extends JpaRepository<Nurse, UUID> {
    boolean existsByUsername(String username);

    /**
     * Look up a nurse by their unique username.  This is useful when mapping
     * authenticated user principals to nurse records.
     *
     * @param username the nurse's username
     * @return an optional nurse record
     */
    java.util.Optional<Nurse> findByUsername(String username);
}
