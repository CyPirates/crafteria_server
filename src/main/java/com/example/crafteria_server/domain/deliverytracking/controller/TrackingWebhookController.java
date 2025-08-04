package com.example.crafteria_server.domain.deliverytracking.controller;

import com.example.crafteria_server.domain.deliverytracking.dto.TrackingWebhookRequest;
import com.example.crafteria_server.domain.deliverytracking.service.DeliveryTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
