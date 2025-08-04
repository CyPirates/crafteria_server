package com.example.crafteria_server.domain.deliverytracking.service;

import com.example.crafteria_server.domain.delivery.entity.Delivery;
import com.example.crafteria_server.domain.delivery.repository.DeliveryRepository;
import com.example.crafteria_server.domain.order.entity.Order;
import com.example.crafteria_server.domain.order.entity.OrderStatus;
import com.example.crafteria_server.domain.order.repository.OrderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "DeliveryTrackingService")
public class DeliveryTrackingService {

    private final DeliveryRepository deliveryRepository;

    @Value("${tracker.delivery.client-id}")
    private String clientId;

    @Value("${tracker.delivery.client-secret}")
    private String clientSecret;

    private static final String TRACKER_URL = "https://apis.tracker.delivery/graphql";

    public void handleTrackingStatusChange(String carrierId, String trackingNumber) {
        try {
            // 1. GraphQL 쿼리 (변수 방식)
            String graphqlQuery = """
                query Track($carrierId: ID!, $trackingNumber: String!) {
                  track(carrierId: $carrierId, trackingNumber: $trackingNumber) {
                    lastEvent {
                      time
                      status {
                        code
                        name
                      }
                      description
                    }
                  }
                }
            """;

            // 2. 변수 설정
            Map<String, Object> variables = new HashMap<>();
            variables.put("carrierId", carrierId);
            variables.put("trackingNumber", trackingNumber);

            // 3. 요청 본문 생성
            Map<String, Object> body = new HashMap<>();
            body.put("query", graphqlQuery);
            body.put("variables", variables);

            // 4. 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "TRACKQL-API-KEY " + clientId + ":" + clientSecret);

            // 5. 요청 실행
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(TRACKER_URL, request, JsonNode.class);

            // 6. 응답 처리
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode lastEvent = response.getBody()
                        .path("data").path("track").path("lastEvent");

                String statusCode = lastEvent.path("status").path("code").asText();
                String statusName = lastEvent.path("status").path("name").asText();

                Optional<Delivery> optionalDelivery = deliveryRepository.findByTrackingNumber(trackingNumber);
                if (optionalDelivery.isEmpty()) {
                    log.warn("⚠️ 해당 운송장번호와 일치하는 Delivery가 없습니다: {}", trackingNumber);
                    return;
                }

                Delivery delivery = optionalDelivery.get();
                Order order = delivery.getOrder();

                if ("delivered".equalsIgnoreCase(statusCode)) {
                    order.setStatus(OrderStatus.DELIVERED);
                } else if ("in_transit".equalsIgnoreCase(statusCode) || "out_for_delivery".equalsIgnoreCase(statusCode)) {
                    order.setStatus(OrderStatus.DELIVERING);
                } else {
                    log.info("🔍 상태 업데이트 없음: statusCode={}, statusName={}", statusCode, statusName);
                    return;
                }

                log.info("📦 주문 상태 업데이트 완료: orderId={}, status={}", order.getId(), order.getStatus());

            } else {
                log.error("❌ Tracker.delivery 응답 오류: {}", response.getBody());
            }

        } catch (HttpClientErrorException e) {
            log.error("🚫 Tracker.delivery API 요청 오류 ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("❗ 운송장 상태 갱신 중 예외 발생", e);
        }
    }
}
