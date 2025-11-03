package com.nckh.yte.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class GeminiProps {

    @Value("${ai.gemini.apikey:}")
    private String apiKey;

    @Value("${ai.gemini.baseurl}")
    private String baseUrl;

    @Value("${ai.gemini.model:gemini-1.5-flash}")
    private String model;
}
