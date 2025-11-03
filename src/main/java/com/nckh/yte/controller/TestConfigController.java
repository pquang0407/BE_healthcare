package com.nckh.yte.controller;

import com.nckh.yte.DrugApiConfig;
import com.nckh.yte.GeminiConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestConfigController {
    private final Environment env;
    private final GeminiConfig gemini;
    private final DrugApiConfig drugApi;

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> config() {
        String port = env.getProperty("local.server.port", env.getProperty("server.port", "8080"));
        return ResponseEntity.ok(Map.of(
            "port", port,
            "datasource.url", env.getProperty("spring.datasource.url"),
            "gemini.baseurl", gemini.getBaseurl(),
            "gemini.model", gemini.getModel(),
            "gemini.apikey_set", gemini.getApikey() != null && !gemini.getApikey().isBlank(),
            "drugapi.baseurl", drugApi.getBaseurl(),
            "drugapi.apikey_set", drugApi.getApikey() != null && !drugApi.getApikey().isBlank()
        ));
    }
}
