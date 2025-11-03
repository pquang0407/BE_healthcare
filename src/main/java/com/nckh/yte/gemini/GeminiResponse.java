// Đặt file này trong: src/main/java/com/nckh/yte/gemini/GeminiResponse.java
package com.nckh.yte.gemini;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GeminiResponse {
    private List<Candidate> candidates;

    @Data
    @NoArgsConstructor
    public static class Candidate {
        private Content content;
    }

    @Data
    @NoArgsConstructor
    public static class Content {
        private List<Part> parts;
    }

    @Data
    @NoArgsConstructor
    public static class Part {
        private String text;
    }

    // Hàm tiện ích để lấy text trả về một cách an toàn
    public String getFirstCandidateText() {
        try {
            return this.candidates.get(0).getContent().getParts().get(0).getText();
        } catch (Exception e) {
            return null;
        }
    }
}