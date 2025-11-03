package com.nckh.yte.service;

import com.nckh.yte.config.GeminiProps;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final GeminiProps gemini;
    private final RestTemplate restTemplate;

    @SuppressWarnings("unchecked")
    public String generateAIResponse(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "❌ Vui lòng nhập nội dung câu hỏi hoặc triệu chứng.";
        }

        // 👋 Bắt lời chào ngắn → trả ngay kiểu “hai người bạn”
        String msg = userMessage.trim();
        if (msg.length() <= 20 && msg.toLowerCase().matches("^(hi|hello|helo|chao|chào|xin chào|yo|ê|e|alo|sup|hey|hí|hì)\\b.*")) {
            String greet = """
            Hello bạn 👋 Mình là bác sĩ AI — nói chuyện kiểu hai đứa bạn cho dễ hiểu nha 😄
            Bạn có thể hỏi thẳng về *thuốc OTC* (không kê đơn) như hạ sốt, giảm đau, xịt mũi, siro ho…
            Để mình tư vấn chuẩn hơn, bạn cho mình vài thông tin cơ bản nè:
            • Tuổi/giới (nếu là nữ: có thai/cho bú không)
            • Triệu chứng chính, bắt đầu khi nào, đang tăng hay giảm
            • Mức độ khó chịu (0–10), có sốt/ho/khó thở/buồn nôn…
            • Bệnh nền/thuốc đang dùng/dị ứng thuốc
            """;
            return sanitizeForChat(greet);
        }

        try {
            // ✅ Endpoint Gemini
            String requestUrl = gemini.getBaseUrl()
                    + gemini.getModel()
                    + ":generateContent?key=" + gemini.getApiKey();

            // =========================
            // 🧠 System prompt (ĐÃ CHỈNH)
            // =========================
            String systemPrompt = """
                Bạn là Bác sĩ AI phong cách Gen Z. Xưng hô “mình – bạn”, nói chuyện thân thiện như hai người bạn.

                Mục tiêu: tư vấn sức khỏe thân thiện và có thể GỢI Ý THUỐC OTC (không cần toa) khi phù hợp.
                Được phép gợi ý nhóm thuốc thông dụng: giảm đau/hạ sốt (paracetamol), kháng viêm không steroid (ibuprofen),
                thuốc nhỏ/xịt mũi co mạch ngắn hạn, nước muối sinh lý, siro ho/long đờm, thuốc dị ứng (kháng histamine),
                men vi sinh, bôi ngoài da nhẹ (hydrocortisone 1%, kẽm oxide), thuốc chống say tàu xe OTC, vitamin/khoáng.
                
                KHÔNG gợi ý hoặc kê: kháng sinh, thuốc corticoid đường uống/tiêm, thuốc tim mạch, thuốc hạ đường huyết,
                thuốc chống động kinh, thuốc an thần/benzodiazepine, thuốc gây nghiện/kiểm soát đặc biệt, thuốc kê đơn nói chung.
                
                Khi nói về thuốc:
                - Trình bày ngắn gọn công dụng + liều dùng cơ bản người lớn (nếu an toàn), kèm cảnh báo TDP thường gặp.
                - Tránh nêu liều cho TRẺ EM, PNCT/cho bú, người có bệnh nền nặng; trong các trường hợp này chỉ nói nguyên tắc an toàn
                  và đề nghị hỏi dược sĩ/bác sĩ để chỉnh liều theo cân nặng/ tình trạng.
                - Luôn thêm câu: “Hãy đọc kỹ hướng dẫn sử dụng và hỏi dược sĩ/bác sĩ nếu không chắc chắn.”

                Phong cách:
                - Câu ngắn, dễ hiểu, 1–2 emoji phù hợp.
                - Tôn trọng, tích cực, tránh thuật ngữ khó.
                - Nếu thông tin thiếu, chỉ hỏi bù TỐI ĐA 1–2 câu, tránh hỏi dồn dập.

                Cấu trúc trả lời (nếu phù hợp):
                1) Nguyên nhân có thể
                2) Thuốc OTC & cách xử lý tại nhà (nêu tên hoạt chất + liều cơ bản người lớn nếu an toàn, kèm cảnh báo)
                3) Khi nào cần gặp bác sĩ
                4) Lưu ý quan trọng

                Nhắc đi khám ngay nếu có dấu hiệu nguy hiểm: khó thở tăng dần, đau ngực dữ dội, sốt cao kéo dài,
                yếu liệt/nói khó, lơ mơ, nôn ói không cầm, chảy máu không cầm, SpO₂ thấp, dị ứng nặng/khó thở, đau bụng dữ dội kèm bụng cứng.
            """;

            // Nội dung người dùng
            Map<String, Object> userContent = Map.of(
                    "role", "user",
                    "parts", List.of(Map.of("text", "Triệu chứng/ câu hỏi về thuốc (xưng hô mình–bạn): " + userMessage))
            );

            // ✅ Body gọi Gemini
            Map<String, Object> requestBody = Map.of(
                    "systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt))),
                    "contents", List.of(userContent),
                    "generationConfig", Map.of(
                            "temperature", 0.6,
                            "maxOutputTokens", 800,
                            "topP", 0.9
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 🛰️ Gọi Gemini
            ResponseEntity<Map> response = restTemplate.postForEntity(requestUrl, entity, Map.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                String detail = (response.getBody() != null) ? response.getBody().toString() : "";
                return sanitizeForChat("❌ Gemini trả về lỗi: " + response.getStatusCodeValue()
                        + (detail.isBlank() ? "" : (" - " + detail)));
            }
            if (response.getBody() == null) return "⚠️ Không nhận được phản hồi từ Gemini.";

            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
            if (candidates == null || candidates.isEmpty()) return "⚠️ Gemini không trả về nội dung.";

            Map<String, Object> candidate0 = candidates.get(0);
            Map<String, Object> content = (Map<String, Object>) candidate0.get("content");
            if (content == null) return "⚠️ Không đọc được phần content từ Gemini.";

            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            if (parts != null && !parts.isEmpty()) {
                Object txt = parts.get(0).get("text");
                String raw = txt != null ? txt.toString() : "";

                // 🛡️ Lọc thô: nếu lỡ gợi ý thuốc kê đơn, thêm cảnh báo mềm (không chặn cứng câu trả lời)
                String safe = softGuardPrescription(raw);

                // 🏥 Gợi ý khoa khám bệnh dựa trên nội dung người dùng. Nếu phát hiện một số
                // triệu chứng hoặc trạng thái đặc biệt (ví dụ mang thai), hệ thống sẽ gợi ý
                // khoa phù hợp để người dùng đến khám. Chỉ áp dụng với bệnh nhân.
                String suggestion = suggestDepartmentForUserMessage(msg);
                if (suggestion != null && !suggestion.isBlank()) {
                    safe = safe + "\n\n🏥 Gợi ý khoa khám bệnh: " + suggestion;
                }

                return sanitizeForChat(safe);
            }

            return "⚠️ Không đọc được phản hồi từ Gemini.";
        } catch (Exception e) {
            e.printStackTrace();
            return sanitizeForChat("❌ Lỗi khi gọi Gemini API: " + e.getMessage());
        }
    }

    /**
     * Làm sạch hiển thị: bỏ markdown cơ bản + loại các ký tự đặc biệt ! # $ % ^ & * + chuẩn hoá xuống dòng
     */
    private String sanitizeForChat(String s) {
        if (s == null) return "";
        String out = s;

        // Chuẩn hoá newline & dọn ký tự điều khiển
        out = out.replace("<EOL>", "\n");
        out = out.replace("\r\n", "\n").replace("\r", "\n");
        out = out.replace("\uFEFF", "");
        out = out.replaceAll("[\\p{Cntrl}&&[^\\n\\t]]", "");
        out = out.replaceAll("\n{3,}", "\n\n").trim();

        // Bỏ markdown cơ bản (nếu UI không render markdown)
        out = out.replaceAll("\\*\\*(.+?)\\*\\*", "$1");     // **bold** -> bold
        out = out.replaceAll("__(.+?)__", "$1");             // __bold__ -> bold
        out = out.replaceAll("(?<!\\*)\\*(?!\\*)(.+?)(?<!\\*)\\*(?!\\*)", "$1"); // *italic* -> italic
        out = out.replaceAll("(?<!_)_(?!_)(.+?)(?<!_)_(?!_)", "$1");             // _italic_ -> italic
        out = out.replaceAll("~~(.+?)~~", "$1");             // ~~strike~~ -> strike
        out = out.replaceAll("`(.+?)`", "$1");               // `code` -> code
        out = out.replaceAll("\\[(.+?)\\)\\((.+?)\\)", "$1"); // [text](url) -> text

        // ❌ Loại các ký tự đặc biệt yêu cầu: ! # $ % ^ & *
        out = out.replaceAll("[!#$%\\^&*]+", "");

        // Thu gọn khoảng trắng dư sau khi xoá ký tự
        out = out.replaceAll("[ \\t]{2,}", " ").trim();
        return out;
    }

    /**
     * 🛡️ Soft-guard: nếu model lỡ gợi ý thuốc kê đơn (kháng sinh, steroid uống, benzo, opioid, v.v.)
     * thì tự động chèn cảnh báo mềm, không phá vỡ mạch hội thoại.
     */
    private String softGuardPrescription(String text) {
        if (text == null || text.isBlank()) return text;

        String lower = text.toLowerCase();

        // Danh sách từ khóa thuốc kê đơn/nhạy cảm (rút gọn, có thể mở rộng dần)
        String[] redFlags = {
                "amoxicillin", "azithromycin", "levofloxacin", "ciprofloxacin", "metronidazole",
                "augmentin", "cephalexin", "cefuroxime", "doxycycline", "clarithromycin",
                "prednisone", "methylprednisolone", "dexamethasone",
                "benzodiazepine", "diazepam", "alprazolam", "clonazepam", "lorazepam",
                "tramadol", "codeine", "morphine", "oxycodone", "fentanyl",
                "isotretinoin", "warfarin", "ssri", "sertraline", "fluoxetine", "escitalopram"
        };

        for (String rf : redFlags) {
            if (lower.contains(rf)) {
                // Chèn cảnh báo một lần là đủ
                if (!lower.contains("thuốc kê đơn") && !lower.contains("không tự ý dùng")) {
                    return text + "\n\nLưu ý: Một số tên thuốc bên trên thuộc nhóm THUỐC KÊ ĐƠN/đặc biệt. "
                            + "Bạn không nên tự ý dùng. Hãy gặp bác sĩ/dược sĩ để được chỉ định phù hợp.";
                }
                break;
            }
        }
        return text;
    }

    /**
     * Gợi ý khoa khám bệnh dựa trên nội dung tin nhắn của người dùng.
     * Phân tích một số từ khóa thông dụng và trả về tên khoa phù hợp.
     * Mặc định trả về null nếu không phát hiện được.
     */
    private String suggestDepartmentForUserMessage(String msg) {
        if (msg == null) return null;
        String lower = msg.toLowerCase();
        // Mang thai hoặc liên quan thai sản
        if (lower.contains("mang thai") || lower.contains("có thai")
            || lower.contains("bầu") || lower.contains("thai kỳ")
            || lower.contains("thai kì")) {
            return "Khoa Sản";
        }
        // Triệu chứng hô hấp: ho, khó thở, viêm phổi
        if (lower.contains("ho") || lower.contains("khó thở")
            || lower.contains("viêm phổi") || lower.contains("đau họng")
            || lower.contains("sổ mũi") || lower.contains("viêm họng")) {
            return "Khoa Hô Hấp";
        }
        // Triệu chứng tiêu hoá: đau bụng, tiêu chảy, táo bón, dạ dày
        if (lower.contains("đau bụng") || lower.contains("tiêu chảy")
            || lower.contains("táo bón") || lower.contains("dạ dày")
            || lower.contains("đau dạ dày") || lower.contains("nôn")
            || lower.contains("buồn nôn")) {
            return "Khoa Tiêu Hóa";
        }
        // Chấn thương, vết thương, đau xương khớp
        if (lower.contains("gãy xương") || lower.contains("vết thương")
            || lower.contains("chấn thương") || lower.contains("đau khớp")
            || lower.contains("đau lưng") || lower.contains("đau vai")
            || lower.contains("đau cổ")) {
            return "Khoa Ngoại";
        }
        // Nội khoa chung: sốt, mệt mỏi, huyết áp, tiểu đường, tim mạch
        if (lower.contains("sốt") || lower.contains("mệt mỏi")
            || lower.contains("huyết áp") || lower.contains("cao huyết áp")
            || lower.contains("tiểu đường") || lower.contains("đường huyết")
            || lower.contains("tim mạch") || lower.contains("đau tim")
            || lower.contains("đau ngực")) {
            return "Khoa Nội";
        }
        // Da liễu: phát ban, mẩn ngứa, dị ứng da
        if (lower.contains("phát ban") || lower.contains("mẩn ngứa")
            || lower.contains("ngứa") || lower.contains("dị ứng da")
            || lower.contains("nổi mề đay")) {
            return "Khoa Da Liễu";
        }
        return null;
    }
}
