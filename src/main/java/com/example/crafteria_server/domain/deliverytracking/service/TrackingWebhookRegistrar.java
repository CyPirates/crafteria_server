package com.example.crafteria_server.domain.deliverytracking.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "TrackingWebhookRegistrar")
public class TrackingWebhookRegistrar {

    @Value("${tracker.delivery.client-id}")     private String clientId;
    @Value("${tracker.delivery.client-secret}") private String clientSecret;
    @Value("${tracker.delivery.webhook-callback-url}") private String callbackUrl;
    @Value("${tracker.delivery.webhook-ttl-hours:48}") private long ttlHours;

    private static final String TRACKER_GQL = "https://apis.tracker.delivery/graphql";
    private final RestTemplate rest = new RestTemplate();

    public void registerTrackWebhook(String carrierId, String trackingNumber) {
        String mutation = """
            mutation RegisterTrackWebhook($input: RegisterTrackWebhookInput!) {
              registerTrackWebhook(input: $input)
            }
        """;

        String expirationTime = OffsetDateTime.now(ZoneOffset.UTC)
                .plusHours(ttlHours).toString();

        Map<String, Object> variables = Map.of(
                "input", Map.of(
                        "carrierId", carrierId,
                        "trackingNumber", trackingNumber,
                        "callbackUrl", callbackUrl,
                        "expirationTime", expirationTime
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "TRACKQL-API-KEY " + clientId + ":" + clientSecret);

        Map<String, Object> body = Map.of("query", mutation, "variables", variables);

        try {
            ResponseEntity<JsonNode> resp = rest.postForEntity(
                    TRACKER_GQL, new HttpEntity<>(body, headers), JsonNode.class);
            log.info("[Webhook 등록/갱신] carrierId={}, tracking={}, status={}",
                    carrierId, trackingNumber, resp.getStatusCode());
        } catch (HttpClientErrorException e) {
            log.error("[Webhook 등록 실패] {} {} : {} {}",
                    carrierId, trackingNumber, e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("[Webhook 등록 예외] {} {}", carrierId, trackingNumber, e);
        }
    }

    public void unregisterTrackWebhook(String carrierId, String trackingNumber) {
        // expirationTime=지금 으로 호출하면 해제
        String mutation = """
            mutation RegisterTrackWebhook($input: RegisterTrackWebhookInput!) {
              registerTrackWebhook(input: $input)
            }
        """;
        String now = OffsetDateTime.now(ZoneOffset.UTC).toString();

        Map<String, Object> variables = Map.of(
                "input", Map.of(
                        "carrierId", carrierId,
                        "trackingNumber", trackingNumber,
                        "callbackUrl", callbackUrl,
                        "expirationTime", now
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "TRACKQL-API-KEY " + clientId + ":" + clientSecret);

        Map<String, Object> body = Map.of("query", mutation, "variables", variables);

        try {
            ResponseEntity<JsonNode> resp = rest.postForEntity(
                    TRACKER_GQL, new HttpEntity<>(body, headers), JsonNode.class);
            log.info("[Webhook 해제] carrierId={}, tracking={}, status={}",
                    carrierId, trackingNumber, resp.getStatusCode());
        } catch (Exception e) {
            log.error("[Webhook 해제 실패] {} {}", carrierId, trackingNumber, e);
        }
    }
}
