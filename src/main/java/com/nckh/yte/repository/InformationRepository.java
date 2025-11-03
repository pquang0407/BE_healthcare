package com.nckh.yte.repository;

import com.nckh.yte.entity.Information;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository cho {@link Information} entity (hiện đang là Drug Cache).
 * Cung cấp phương thức tìm kiếm cache theo tên thuốc.
 */
public interface InformationRepository extends JpaRepository<Information, UUID> {

    /**
     * Tìm một bản ghi cache dựa trên tên thuốc (tra cứu chính xác, phân biệt chữ hoa/thường).
     * @param name Tên thuốc đã nhập.
     * @return Optional chứa bản ghi cache nếu tìm thấy.
     */
    Optional<Information> findByName(String name);
}