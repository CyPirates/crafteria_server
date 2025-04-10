package com.example.crafteria_server.domain.delivery.controller;

import com.example.crafteria_server.domain.delivery.dto.DeliveryDto;
import com.example.crafteria_server.domain.delivery.entity.Delivery;
import com.example.crafteria_server.domain.delivery.service.DeliveryService;
import com.example.crafteria_server.global.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryController {
    private final DeliveryService deliveryService;

    @PostMapping(consumes = "multipart/form-data")
    @Operation(summary = "배송 등록", description = "배송 정보를 등록합니다.")
    public ResponseEntity<DeliveryDto.DeliveryResponse> createDelivery(@AuthenticationPrincipal PrincipalDetails principalDetails, @RequestBody DeliveryDto.DeliveryRequest deliveryRequest) throws AccessDeniedException {
        DeliveryDto.DeliveryResponse deliveryResponse = deliveryService.createDelivery(deliveryRequest, principalDetails.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(deliveryResponse);
    }

    @PutMapping(value = "/{deliveryId}", consumes = "multipart/form-data")
    @Operation(summary = "배송 수정", description = "배송 정보를 수정합니다.")
    public ResponseEntity<DeliveryDto.DeliveryResponse> updateDelivery(@PathVariable Long deliveryId, @AuthenticationPrincipal PrincipalDetails principalDetails, @RequestBody DeliveryDto.DeliveryRequest deliveryRequest) throws AccessDeniedException {
        DeliveryDto.DeliveryResponse deliveryResponse = deliveryService.updateDelivery(deliveryId, deliveryRequest, principalDetails.getUser());
        return ResponseEntity.ok(deliveryResponse);
    }

    @DeleteMapping(value = "/{deliveryId}")
    @Operation(summary = "배송 삭제", description = "배송 정보를 삭제합니다.")
    public ResponseEntity<Void> deleteDelivery(@PathVariable Long deliveryId, @AuthenticationPrincipal PrincipalDetails principalDetails) throws AccessDeniedException {
        deliveryService.deleteDelivery(deliveryId, principalDetails.getUser());
        return ResponseEntity.ok().build();
    }
}
