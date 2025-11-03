// Đặt file này trong: src/main/java/com/nckh/yte/service/GeminiService.java
package com.nckh.yte.service;

import com.nckh.yte.config.GeminiProps;
import com.nckh.yte.gemini.GeminiRequest;
import com.nckh.yte.gemini.GeminiResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor // Tự động tiêm (inject) các dependency 'final'
public class GeminiService {

    private final RestTemplate restTemplate;
    private final GeminiProps geminiProps; // Tiêm cấu hình Gemini bạn đã tạo

    private String fullApiUrl; // Biến để lưu URL đầy đủ

    /**
     * Hàm này tự động chạy sau khi service được tạo.
     * Nó xây dựng URL API đầy đủ từ cấu hình của bạn.
     */
    @PostConstruct
    public void init() {
        // Ví dụ: "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"
        this.fullApiUrl = geminiProps.getBaseUrl() + geminiProps.getModel() + ":generateContent";
        
        System.out.println("======================================================");
        System.out.println("Gemini API URL Initialized: " + this.fullApiUrl);
        System.out.println("======================================================");
    }

    /**
     * Gửi triệu chứng đến Gemini và nhận về chuyên khoa phù hợp.
     * @param symptom Mô tả triệu chứng của bệnh nhân.
     * @return Tên chuyên khoa (ví dụ: "Tim mạch") hoặc null nếu thất bại.
     */
    public String determineSpecialtyFromSymptom(String symptom) {
        // 1. Tạo Prompt (Hướng dẫn cho Gemini)
        String prompt = String.format(
            "Bạn là một trợ lý y tế thông minh. Dựa trên mô tả triệu chứng sau đây, " +
            "hãy trả về CHÍNH XÁC MỘT chuyên khoa y tế phù hợp nhất. " +
            "KHÔNG GIẢI THÍCH, KHÔNG THÊM BẤT KỲ CHỮ NÀO KHÁC. " +
            "Các chuyên khoa hợp lệ ví dụ: 'Tim mạch', 'Cơ xương khớp', 'Tai mũi họng', 'Da liễu', 'Răng hàm mặt', 'Nội tiết', 'Tiêu hóa', 'Đa khoa'.\n\n" +
            "Triệu chứng: \"%s\"",
            symptom
        );

        // 2. Tạo Request Body
        GeminiRequest.Part part = new GeminiRequest.Part(prompt);
        GeminiRequest.Content content = new GeminiRequest.Content(List.of(part));
        GeminiRequest requestBody = new GeminiRequest(List.of(content));

        // 3. Tạo Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Lấy API Key từ file cấu hình (GeminiProps)
        headers.set("x-goog-api-key", geminiProps.getApiKey());

        // 4. Gọi API
        HttpEntity<GeminiRequest> entity = new HttpEntity<>(requestBody, headers);

        try {
            // Sử dụng URL đã được xây dựng trong hàm init()
            GeminiResponse response = restTemplate.postForObject(this.fullApiUrl, entity, GeminiResponse.class);

            if (response != null && response.getFirstCandidateText() != null) {
                String specialty = response.getFirstCandidateText().trim();
                // Dọn dẹp kết quả (loại bỏ dấu ngoặc kép hoặc markdown)
                specialty = specialty.replace("\"", "").replace("*", "").trim();
                return specialty;
            }
        } catch (Exception e) {
            // Nếu có lỗi (API key sai, hết hạn, ...), log lỗi và trả về null
            System.err.println("Lỗi khi gọi Gemini API: " + e.getMessage());
            return null; 
        }
        
        return null; // Trả về null để logic cũ (fallback) được thực thi
    }
}