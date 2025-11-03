package com.nckh.yte.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

/**
 * Entity này hiện được sử dụng làm bộ đệm (cache) cho các kết quả tra cứu thuốc.
 * Nó lưu trữ tên thuốc (đã tìm kiếm) và toàn bộ nội dung JSON phản hồi
 * đã được xử lý (bao gồm cả tóm tắt của OpenAI).
 */
@Entity
@Table(name = "information") // Vẫn giữ tên bảng "information" theo yêu cầu
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Information {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Tên thuốc mà người dùng đã nhập để tìm kiếm (ví dụ: "Paracetamol").
     * Được đặt là unique để đảm bảo không có 2 cache cho cùng 1 tên thuốc.
     */
    @Column(name = "drug_name", nullable = false, unique = true, length = 500)
    private String name;

    /**
     * Chuỗi JSON chứa toàn bộ phản hồi (Map<String, Object>) được trả về cho FE.
     * Sử dụng kiểu TEXT để lưu trữ chuỗi JSON lớn.
     */
    @Column(name = "response_json", nullable = false, columnDefinition = "TEXT")
    private String responseData;
}