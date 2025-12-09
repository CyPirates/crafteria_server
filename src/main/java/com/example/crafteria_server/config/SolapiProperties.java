package com.example.crafteria_server.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "solapi")
@Getter
@Setter
public class SolapiProperties {
    private String apiKey;
    private String apiSecret;
    private String from;
}
