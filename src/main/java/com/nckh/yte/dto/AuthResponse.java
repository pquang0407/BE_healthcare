package com.nckh.yte.dto;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    /**
     * JWT để client gửi trong header Authorization cho các request tiếp theo.
     */
    private String token;

    /**
     * Tên hiển thị cho người dùng (ưu tiên fullName; nếu trống dùng username).
     */
    private String fullName;

    /**
     * Vai trò chính của người dùng cho FE (không prefix),
     * ví dụ: "PATIENT", "DOCTOR", "NURSE", "ADMIN".
     */
    private String role;
}
