package com.example.crafteria_server.domain.delivery.controller;

import com.example.crafteria_server.domain.delivery.dto.DeliveryDto;
import com.example.crafteria_server.domain.delivery.entity.Delivery;
import com.example.crafteria_server.domain.delivery.service.DeliveryService;
import com.example.crafteria_server.global.response.JsonBody;
import com.example.crafteria_server.global.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
@Tag(name = "배송", description = "배송 관련 API")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @Operation(summary = "배송 등록", description = "배송 정보를 등록합니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JsonBody<DeliveryDto.DeliveryResponse>> createDelivery(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @ModelAttribute DeliveryDto.DeliveryRequest deliveryRequest) throws AccessDeniedException {

        DeliveryDto.DeliveryResponse response = deliveryService.createDelivery(deliveryRequest, principalDetails.getUser());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(JsonBody.of(201, "배송 등록 성공", response));
    }

    @Operation(summary = "배송 수정", description = "배송 정보를 수정합니다.")
    @PutMapping(value = "/{deliveryId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JsonBody<DeliveryDto.DeliveryResponse>> updateDelivery(
            @PathVariable Long deliveryId,
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @ModelAttribute DeliveryDto.DeliveryRequest deliveryRequest) throws AccessDeniedException {

        DeliveryDto.DeliveryResponse response = deliveryService.updateDelivery(deliveryId, deliveryRequest, principalDetails.getUser());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(JsonBody.of(200, "배송 수정 성공", response));
    }

    @Operation(summary = "배송 삭제", description = "배송 정보를 삭제합니다.")
    @DeleteMapping("/{deliveryId}")
    public ResponseEntity<JsonBody<Void>> deleteDelivery(
            @PathVariable Long deliveryId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) throws AccessDeniedException {

        deliveryService.deleteDelivery(deliveryId, principalDetails.getUser());
        return ResponseEntity
                .ok(JsonBody.of(200, "배송 삭제 성공", null));
    }

    @Operation(summary = "배송 단건 조회", description = "배송 ID로 배송 정보를 조회합니다.")
    @GetMapping("/{deliveryId}")
    public ResponseEntity<JsonBody<DeliveryDto.DeliveryResponse>> getDeliveryById(
            @PathVariable Long deliveryId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) throws AccessDeniedException {

        DeliveryDto.DeliveryResponse response = deliveryService.getDeliveryById(deliveryId, principalDetails.getUser());
        return ResponseEntity
                .ok(JsonBody.of(200, "배송 조회 성공", response));
    }

    @Operation(summary = "내 배송 목록 조회", description = "내가 등록한 모든 배송 정보를 조회합니다.")
    @GetMapping("/my")
    public ResponseEntity<JsonBody<List<DeliveryDto.DeliveryResponse>>> getMyDeliveries(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        List<DeliveryDto.DeliveryResponse> responseList = deliveryService.getMyDeliveries(principalDetails.getUser());
        return ResponseEntity
                .ok(JsonBody.of(200, "배송 목록 조회 성공", responseList));
    }
}
