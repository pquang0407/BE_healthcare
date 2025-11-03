package com.nckh.yte.controller;

import com.nckh.yte.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * Chat với AI (Gemini)
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, String> body) {
        String message = body != null ? body.get("message") : null;
        String reply = chatService.generateAIResponse(message);
        if (reply == null) reply = "⚠️ Không nhận được phản hồi từ AI.";

        // Xây JSON tương thích Gemini (Java 8 dùng Collections + Arrays)
        Map<String, Object> part = new HashMap<>();
        part.put("text", reply);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", Arrays.asList(part));

        Map<String, Object> candidate = new HashMap<>();
        candidate.put("content", content);

        Map<String, Object> resp = new HashMap<>();
        resp.put("reply", reply);
        resp.put("candidates", Arrays.asList(candidate));

        return ResponseEntity.ok(resp);
    }

    /**
     * Ping để kiểm tra server đang hoạt động
     */
    @GetMapping("/chat/ping")
    public ResponseEntity<Map<String, String>> ping() {
        Map<String, String> map = new HashMap<>();
        map.put("status", "✅ Chat API is running");
        return ResponseEntity.ok(map);
    }

    /**
     * Gửi tin nhắn giữa người dùng và bác sĩ (demo)
     */
    @PostMapping("/chat/send")
    public ResponseEntity<Map<String, String>> sendChat(@RequestBody Map<String, String> body) {
        String senderId = body != null ? body.get("senderId") : null;
        String receiverId = body != null ? body.get("receiverId") : null;
        String content = body != null ? body.get("content") : null;

        Map<String, String> resp = new HashMap<>();
        resp.put("message", "Tin nhắn đã được gửi thành công");
        resp.put("senderId", senderId != null ? senderId : "");
        resp.put("receiverId", receiverId != null ? receiverId : "");
        resp.put("content", content != null ? content : "");

        return ResponseEntity.ok(resp);
    }
}
