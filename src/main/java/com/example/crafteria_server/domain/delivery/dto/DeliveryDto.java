package com.example.crafteria_server.domain.delivery.dto;

import com.example.crafteria_server.domain.delivery.entity.Delivery;
import com.example.crafteria_server.domain.order.entity.Order;
import com.example.crafteria_server.domain.order.entity.OrderItem;
import com.example.crafteria_server.domain.order.entity.OrderStatus;
import com.example.crafteria_server.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

        @Schema(description = "주문 ID", example = "100")
        private Long orderId;

        @Schema(description = "고객명", example = "홍길동")
        private String customerName;

        @Schema(description = "배송지 주소", example = "서울시 강남구 테헤란로 123")
        private String deliveryAddress;


        @Schema(description = "주문일", example = "2025-07-03T15:20:30")
        private LocalDateTime orderDate;

        @Schema(description = "운송장 번호", example = "1234567890")
        private String trackingNumber;

        @Schema(description = "택배사", example = "우체국택배")
        private String courier;

        @Schema(description = "주문 상태", example = "DELIVERING")
        private OrderStatus orderStatus;

        @Schema(description = "배송일", example = "2025-07-04T11:11:11")
        private LocalDateTime deliveryDate;

        public static DeliveryResponse from(Delivery delivery) {
            Order order = delivery.getOrder();
            User user = order.getUser();

            return DeliveryResponse.builder()
                    .deliveryId(delivery.getId())
                    .orderId(order.getId())
                    .customerName(user.getRealname())
                    .deliveryAddress(order.getDeliveryAddress())
                    .orderDate(order.getCreateDate())
                    .trackingNumber(delivery.getTrackingNumber())
                    .courier(delivery.getCourier())
                    .orderStatus(order.getStatus())
                    .deliveryDate(delivery.getCreateDate())
                    .build();
        }

    }


}
