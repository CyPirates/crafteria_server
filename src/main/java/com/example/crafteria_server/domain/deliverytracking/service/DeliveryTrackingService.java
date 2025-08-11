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
    private final RestTemplate rest = new RestTemplate();

    @Value("${tracker.delivery.client-id}")
    private String clientId;

    @Value("${tracker.delivery.client-secret}")
    private String clientSecret;

    private static final String TRACKER_GQL = "https://apis.tracker.delivery/graphql";

    @Transactional
    public void handleTrackingStatusChange(String carrierId, String trackingNumber) {
        try {
            String query = """
                query Track($carrierId: ID!, $trackingNumber: String!) {
                  track(carrierId: $carrierId, trackingNumber: $trackingNumber) {
                    lastEvent {
                      time
                      status { code name }
                      description
                    }
                  }
                }
            """;

            Map<String, Object> variables = Map.of(
                    "carrierId", carrierId,
                    "trackingNumber", trackingNumber
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "TRACKQL-API-KEY " + clientId + ":" + clientSecret);

            Map<String, Object> body = Map.of("query", query, "variables", variables);

            ResponseEntity<JsonNode> resp = rest.postForEntity(
                    TRACKER_GQL, new HttpEntity<>(body, headers), JsonNode.class);

            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                log.error("[TrackingAPI] 비정상 응답: {}", resp);
                return;
            }

            JsonNode lastEvent = resp.getBody().path("data").path("track").path("lastEvent");
            if (lastEvent.isMissingNode() || lastEvent.isNull()) {
                log.info("[TrackingAPI] lastEvent 없음: carrierId={}, tracking={}", carrierId, trackingNumber);
                return;
            }

            String code = lastEvent.path("status").path("code").asText(null);
            if (code == null) {
                log.info("[TrackingAPI] status.code 없음: {}", lastEvent);
                return;
            }

            deliveryRepository.findByTrackingNumber(trackingNumber).ifPresent(delivery -> {
                Order order = delivery.getOrder();

                // 코드 매핑: 필요시 추가 코드(in_delivery_started 등)도 덧붙이면 됨
                if ("delivered".equalsIgnoreCase(code)) {
                    if (order.getStatus() != OrderStatus.DELIVERED) {
                        order.setStatus(OrderStatus.DELIVERED);
                        log.info("[주문상태] DELIVERED로 갱신 - orderId={}", order.getId());
                    }
                } else if ("in_transit".equalsIgnoreCase(code) || "out_for_delivery".equalsIgnoreCase(code)) {
                    if (order.getStatus() != OrderStatus.DELIVERING) {
                        order.setStatus(OrderStatus.DELIVERING);
                        log.info("[주문상태] DELIVERING으로 갱신 - orderId={}", order.getId());
                    }
                } else {
                    // 그 외 상태는 스킵 (필요하면 추가 매핑)
                    log.debug("[주문상태] 갱신 없음 - code={}", code);
                }
                // 트랜잭션 내라 save 호출 불필요(JPA flush)지만, 명시적으로 넣고 싶다면 레포지토리 통해 save 가능
            });

        } catch (HttpClientErrorException e) {
            log.error("[TrackingAPI 오류] {} : {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("[Tracking 처리 예외]", e);
        }
    }


}
