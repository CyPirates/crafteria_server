package com.example.crafteria_server.domain.order.dto;

import com.example.crafteria_server.domain.order.entity.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

public class OrderDto {
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderResponse {
        @NotNull
        @Schema(description = "유저 ID", example = "1")
        private long userId;

        @NotNull
        @Schema(description = "도면 ID", example = "1")
        private long modelId;

        @NotNull
        @Schema(description = "구매 가격", example = "5000")
        private long purchasePrice;

        @NotNull
        @Schema(description = "제조사 ID", example = "1")
        private long manufacturerId;  // 제조사 ID 추가

        @NotNull
        @Schema(description = "가로 사이즈", example = "10.0")
        private double widthSize;

        @NotNull
        @Schema(description = "세로 사이즈", example = "10.0")
        private double lengthSize;

        @NotNull
        @Schema(description = "높이 사이즈", example = "10.0")
        private double heightSize;

        @NotNull
        @Schema(description = "배율" , example = "1.0")
        private double magnification;

        @NotNull
        @Schema(description = "배송 주소", example = "서울시 강남구 역삼동 123-456 101호")
        private String deliveryAddress;

        @NotNull
        @Schema(description = "구매 상태", example = "ORDERED")
        private String status;

        public static OrderResponse from(Order order) {
            return OrderResponse.builder()
                    .userId(order.getUser().getId())
                    .modelId(order.getModel().getId())
                    .purchasePrice(order.getPurchasePrice())
                    .deliveryAddress(order.getDeliveryAddress())
                    .manufacturerId(order.getManufacturer().getId())  // 제조사 ID 추가
                    .widthSize(order.getWidthSize())
                    .lengthSize(order.getLengthSize())
                    .heightSize(order.getHeightSize())
                    .status(order.getStatus().name())
                    .build();
        }
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderRequest {
        @NotNull
        @Schema(description = "도면 ID", example = "1")
        private long modelId;

        @NotNull
        @Schema(description = "제조사 ID", example = "1")
        private long manufacturerId;  // 제조사 ID 추가

        @NotNull
        @Schema(description = "가로 사이즈", example = "10.0")
        private double widthSize;

        @NotNull
        @Schema(description = "세로 사이즈", example = "10.0")
        private double lengthSize;

        @NotNull
        @Schema(description = "높이 사이즈", example = "10.0")
        private double heightSize;

        @NotNull
        @Schema(description = "배율" , example = "1.0")
        private double magnification;

        @NotNull
        @Schema(description = "배송지", example = "서울시 강남구 역삼동 123-456 101호")
        private String deliveryAddress;
    }
}
