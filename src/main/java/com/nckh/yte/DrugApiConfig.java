package com.nckh.yte;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ai.drugapi")
public class DrugApiConfig {
    private String baseurl;
    private String apikey;
}
