package com.example.crafteria_server.config;

import com.example.crafteria_server.config.SolapiProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SolapiSmsClient {

    private final SolapiProperties solapiProperties;
    private final ObjectMapper objectMapper;

    private static final String SEND_URL = "https://api.solapi.com/messages/v4/send-many/detail";

    public void sendSimpleMessage(String to, String text) {
        try {
            String from = solapiProperties.getFrom();

            // ✅ Solapi가 요구하는 형식: "messages": [ { ... } ]
            Map<String, Object> payload = Map.of(
                    "messages", java.util.List.of(
                            Map.of(
                                    "to", to,
                                    "from", from,
                                    "text", text
                            )
                    )
            );

            String json = objectMapper.writeValueAsString(payload);
            String authHeader = SolapiAuth.createAuthHeader(
                    solapiProperties.getApiKey(),
                    solapiProperties.getApiSecret()
            );

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.solapi.com/messages/v4/send-many/detail"))
                    .header("Authorization", authHeader)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() / 100 != 2) {
                log.error("[Solapi] 문자 발송 실패 status={}, body={}",
                        response.statusCode(), response.body());
            } else {
                log.info("[Solapi] 문자 발송 성공 body={}", response.body());
            }
        } catch (Exception e) {
            log.error("[Solapi] 문자 발송 중 예외", e);
        }
    }
}
