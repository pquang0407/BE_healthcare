package com.nckh.yte.repository;

import com.nckh.yte.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

/**
 * Repository for accessing user data.
 * Supports querying by username and filtering by role.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    /**
     * Find all users having the given role name (case-insensitive).
     * Example: findByRole_NameIgnoreCase("PATIENT")
     */
    List<User> findByRole_NameIgnoreCase(String roleName);
}
