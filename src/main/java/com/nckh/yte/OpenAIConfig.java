package com.nckh.yte;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.openai")
public class OpenAIConfig {
    private String model;
    private String apikey;
    private String baseurl;
    private String organization; // optional
    private String project;      // optional
}
