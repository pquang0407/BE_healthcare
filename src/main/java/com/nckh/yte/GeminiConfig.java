package com.nckh.yte;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ai.gemini")
public class GeminiConfig {
    // KHỚP yml: baseurl/model/apikey (không dùng dấu gạch)
    private String baseurl;
    private String model;
    private String apikey;
}
