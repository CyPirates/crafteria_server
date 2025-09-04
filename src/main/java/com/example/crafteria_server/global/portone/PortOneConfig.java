package com.example.crafteria_server.global.portone;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PortOneConfig {
    @Bean
    public WebClient portOneWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.portone.io")
                .build();
    }
}
