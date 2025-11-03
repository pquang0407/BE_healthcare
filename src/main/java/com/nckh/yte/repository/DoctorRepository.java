package com.nckh.yte.repository;

import com.nckh.yte.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; // ✅ Thêm import (hoặc bỏ java.util. ở dưới)
import java.util.Optional; // ✅ Thêm import (hoặc bỏ java.util. ở dưới)
import java.util.UUID; // ✅ Thêm import

// ✅ Sửa: Long -> UUID
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {
    boolean existsByUsername(String username);

    /**
     * Look up a doctor by their unique username.  This is useful when mapping
     * authenticated user principals to doctor records.
     *
     * @param username the doctor's username
     * @return an optional doctor record
     */
    // ✅ (Bỏ java.util.)
    Optional<Doctor> findByUsername(String username);

    /**
     * Find doctors by specialty name ignoring case.  Although the current
     * {@link Doctor} entity does not define a {@code specialty} property, this
     * method is provided for future compatibility.  It will only be used if
     * such a property is added later.
     *
     * @param specialty the specialty to search for
     * @return list of matching doctors
     */
    // ✅ (Bỏ java.util.)
    List<Doctor> findBySpecialtyIgnoreCase(String specialty);
}