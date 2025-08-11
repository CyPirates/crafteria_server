package com.example.crafteria_server.domain.deliverytracking.controller;

import com.example.crafteria_server.domain.deliverytracking.dto.TrackingWebhookRequest;
import com.example.crafteria_server.domain.deliverytracking.service.DeliveryTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/tracking")
@RequiredArgsConstructor
@Slf4j(topic = "TrackingWebhookController")
public class TrackingWebhookController {

    private final DeliveryTrackingService deliveryTrackingService;

    @PostMapping("/webhook")  // <- 중복된 '/api/v1/tracking' 제거
    public ResponseEntity<Void> onTrackingStatusChanged(@RequestBody TrackingWebhookRequest request) {
        deliveryTrackingService.handleTrackingStatusChange(request.getCarrierId(), request.getTrackingNumber());
        return ResponseEntity.accepted().build();
    }


    @PostMapping
    public ResponseEntity<Void> onTrackingStatusChanged(
            @RequestHeader(value = "X-Webhook-Secret", required = false) String secret,
            @RequestBody TrackingWebhookRequest req) {


        if (req.getCarrierId() == null || req.getTrackingNumber() == null) {
            return ResponseEntity.badRequest().build();
        }

        // 빠르게 202로 응답, 내부 처리 진행
        deliveryTrackingService.handleTrackingStatusChange(req.getCarrierId(), req.getTrackingNumber());
        return ResponseEntity.accepted().build();
    }
}
