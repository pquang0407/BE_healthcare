package com.nckh.yte.dto;
import lombok.*;

@Getter @Setter
public class RegisterRequest {
    private String username;
    private String password;
    private String fullName; // tuỳ chọn; nếu FE chưa có có thể để null/"" và fallback sang username
}
