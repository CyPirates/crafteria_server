package com.example.crafteria_server.domain.order.dto;

import com.example.crafteria_server.domain.file.entity.File;
import com.example.crafteria_server.domain.order.entity.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

public class OrderDto {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderResponse {
        @NotNull
        @Schema(description = "주문 ID", example = "1")
        private Long orderId;

        @NotNull
        @Schema(description = "유저 ID", example = "1")
        private long userId;

        @NotNull
        @Schema(description = "제조사 ID", example = "1")
        private long manufacturerId;

        @NotNull
        @Schema(description = "구매 가격", example = "5000")
        private long purchasePrice;

        @NotNull
        @Schema(description = "주문 상태", example = "ORDERED")
        private String status;

        @Schema(description = "모델 파일 URL 리스트")
        private List<String> modelFileUrls; // 여러 도면 파일의 URL 리스트

        @NotNull
        @Schema(description = "배송 주소", example = "서울시 강남구 역삼동 123-456")
        private String deliveryAddress;

        @NotNull
        @Schema(description = "받는 사람 이름", example = "홍길동")
        private String recipientName;

        @NotNull
        @Schema(description = "받는 사람 전화번호", example = "010-1234-5678")
        private String recipientPhone;

        @NotNull
        @Schema(description = "받는 사람 이메일", example = "test@example.com")
        private String recipientEmail;

        @Schema(description = "특별 요청 사항", example = "부재시 경비실에 맡겨주세요")
        private String specialRequest;

        @Schema(description = "주문 아이템 리스트")
        private List<OrderItemDto> orderItems;

        public static OrderResponse from(Order order) {
            return OrderResponse.builder()
                    .orderId(order.getId())
                    .userId(order.getUser().getId())
                    .manufacturerId(order.getManufacturer().getId())
                    .purchasePrice(order.getPurchasePrice())
                    .status(order.getStatus().getKey())
                    .modelFileUrls(order.getOrderItems().stream()
                            .map(orderItem -> orderItem.getFile().getUrl())  // 파일 URL 직접 접근
                            .collect(Collectors.toList()))
                    .deliveryAddress(order.getDeliveryAddress())
                    .recipientName(order.getRecipientName())
                    .recipientPhone(order.getRecipientPhone())
                    .recipientEmail(order.getRecipientEmail())
                    .specialRequest(order.getSpecialRequest())
                    .orderItems(order.getOrderItems().stream()
                            .map(orderItem -> OrderItemDto.builder()
                                    .widthSize(orderItem.getWidthSize())
                                    .lengthSize(orderItem.getLengthSize())
                                    .heightSize(orderItem.getHeightSize())
                                    .magnification(orderItem.getMagnification())
                                    .quantity(orderItem.getQuantity())
                                    .technologyId(orderItem.getTechnology().getId())
                                    .build())
                            .collect(Collectors.toList()))
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
        @Schema(description = "제조사 ID", example = "1")
        private long manufacturerId;

        @NotNull
        @Schema(description = "구매 가격", example = "5000")
        private long purchasePrice;

        @NotNull
        @Schema(description = "주문 상태", example = "ORDERED")
        private String status;

        @NotNull
        @Schema(description = "배송 주소", example = "서울시 강남구 역삼동 123-456")
        private String deliveryAddress;

        @NotNull
        @Schema(description = "주문 아이템 리스트")
        private List<OrderItemDto> orderItems;

        @NotNull
        @Schema(description = "받는 사람 이름", example = "홍길동")
        private String recipientName;

        @NotNull
        @Schema(description = "받는 사람 전화번호", example = "010-1234-5678")
        private String recipientPhone;

        @NotNull
        @Schema(description = "받는 사람 이메일", example = "example@naver.com")
        private String recipientEmail;

        @Schema(description = "특별 요청 사항", example = "부재시 경비실에 맡겨주세요")
        private String specialRequest;

    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDto {


        @Schema(description = "가로 사이즈", example = "10.0")
        private double widthSize;


        @Schema(description = "세로 사이즈", example = "10.0")
        private double lengthSize;


        @Schema(description = "높이 사이즈", example = "10.0")
        private double heightSize;


        @Schema(description = "배율", example = "1.0")
        private double magnification;


        @Schema(description = "주문 수량", example = "1")
        private int quantity;

        @Schema(description = "기술 ID", example = "1")
        private Long technologyId;

        @Override
        public String toString() {
            return "OrderItemDto{" +
                    "widthSize=" + widthSize +
                    ", lengthSize=" + lengthSize +
                    ", heightSize=" + heightSize +
                    ", magnification=" + magnification +
                    ", quantity=" + quantity +
                    ", technologyId=" + technologyId +
                    '}';
        }
    }
}
