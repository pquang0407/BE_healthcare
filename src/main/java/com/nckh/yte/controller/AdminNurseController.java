package com.nckh.yte.controller;

import com.nckh.yte.entity.Nurse;
import com.nckh.yte.service.NurseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller cho chức năng quản lý y tá.  Đặt đường dẫn giống như
 * {@link AdminDoctorController} để frontend có thể gọi API
 * tương đồng (ví dụ: POST /api/admin/create-nurse).
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public record AdminNurseController(NurseService service) {

    /**
     * Lớp request cho việc tạo y tá. Sử dụng cùng tên thuộc tính với
     * DTO của bác sĩ để tránh nhầm lẫn.
     *
     * @param fullName Họ tên y tá
     * @param username Tên đăng nhập duy nhất
     * @param password Mật khẩu plain text (sẽ được mã hoá ở service)
     */
    record Req(String fullName, String username, String password) {}

    /**
     * Endpoint tạo y tá mới. Đường dẫn và prefix giống với endpoint
     * tạo bác sĩ: POST /api/admin/create-nurse.  Nếu tạo thành công
     * trả về thông tin cơ bản, ngược lại trả về lỗi phù hợp.
     */
    @PostMapping("/create-nurse")
    public ResponseEntity<?> createNurse(@RequestBody Req req) {
        try {
            Nurse nurse = service.create(req.fullName(), req.username(), req.password());
            return ResponseEntity.ok(Map.of(
                    "message", "Tạo y tá thành công!",
                    "id", nurse.getId(),
                    "fullName", nurse.getFullName(),
                    "username", nurse.getUsername()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(409).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Lỗi server khi tạo y tá: " + e.getMessage()));
        }
    }
}
