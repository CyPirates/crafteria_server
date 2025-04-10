package com.example.crafteria_server.domain.delivery.dto;

import com.example.crafteria_server.domain.delivery.entity.Delivery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class DeliveryDto {
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryRequest {
        @NotNull
        @Schema(description = "주문 ID", example = "1")
        private Long orderId;

        @NotNull
        @Schema(description = "택배사", example = "우체국택배")
        private String courier;

        @NotNull
        @Schema(description = "송장번호", example = "1234567890")
        private String trackingNumber;

    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryResponse {
        @Schema(description = "배송 ID", example = "1")
        private Long deliveryId;

        @Schema(description = "주문 ID", example = "1")
        private Long orderId;

        @Schema(description = "택배사", example = "우체국택배")
        private String courier;

        @Schema(description = "송장번호", example = "1234567890")
        private String trackingNumber;

        @Schema(description = "배송 날짜", example = "2023-01-15T12:34:56")
        private LocalDateTime deliveryDate; // 주문 날짜 필드 추가


        public static DeliveryResponse from(Delivery delivery) {
            return DeliveryResponse.builder()
                    .deliveryId(delivery.getId())
                    .orderId(delivery.getOrder().getId())
                    .courier(delivery.getCourier())
                    .trackingNumber(delivery.getTrackingNumber())
                    .deliveryDate(delivery.getCreateDate())
                    .build();
        }
    }
}
