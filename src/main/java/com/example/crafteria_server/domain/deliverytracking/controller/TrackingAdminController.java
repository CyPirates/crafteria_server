package com.example.crafteria_server.domain.deliverytracking.controller;

import com.example.crafteria_server.domain.deliverytracking.service.DeliveryBackfillService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/tracking")
@RequiredArgsConstructor
public class TrackingAdminController {

    private final DeliveryBackfillService backfillService;

    @PostMapping("/webhook-backfill")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> backfill() {
        int registered = backfillService.registerAllExistingDeliveriesForWebhook();
        return ResponseEntity.ok(Map.of("registered", registered));
    }
}
