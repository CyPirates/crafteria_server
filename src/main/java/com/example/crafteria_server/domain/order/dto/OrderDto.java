package com.example.crafteria_server.domain.order.dto;

import com.example.crafteria_server.domain.file.entity.File;
import com.example.crafteria_server.domain.order.entity.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
        @Schema(description = "구매 가격", example = "5000")
        private long purchasePrice;

        @NotNull
        @Schema(description = "제조사 ID", example = "1")
        private long manufacturerId;

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
        @Schema(description = "배율", example = "1.0")
        private double magnification;

        @NotNull
        @Schema(description = "주문 수량", example = "1")
        private int quantity;

        @NotNull
        @Schema(description = "배송 주소", example = "서울시 강남구 역삼동 123-456 101호")
        private String deliveryAddress;

        @NotNull
        @Schema(description = "구매 상태", example = "ORDERED")
        private String status;

        @Schema(description = "모델 파일 URL 리스트")
        private List<String> modelFileUrls; // 여러 도면 파일의 URL 리스트

        @NotNull
        @Schema(description = "받는 사람 이름", example = "홍길동")
        private String recipientName;

        @NotNull
        @Schema(description = "받는 사람 전화번호", example = "010-1234-5678")
        private String recipientPhone;

        @NotNull
        @Schema(description = "받는 사람 이메일", example = "test@example.com")
        private String recipientEmail;

        @Schema(description = "요청 사항", example = "부재 시 경비실에 맡겨주세요.")
        private String specialRequest;

        public static OrderResponse from(Order order) {
            return OrderResponse.builder()
                    .orderId(order.getId())
                    .userId(order.getUser().getId())
                    .purchasePrice(order.getPurchasePrice())
                    .deliveryAddress(order.getDeliveryAddress())
                    .manufacturerId(order.getManufacturer().getId())
                    .widthSize(order.getWidthSize())
                    .lengthSize(order.getLengthSize())
                    .heightSize(order.getHeightSize())
                    .status(order.getStatus().getKey())
                    .magnification(order.getMagnification())
                    .quantity(order.getQuantity())
                    .modelFileUrls(order.getModelFiles().stream().map(File::getUrl).toList())
                    .recipientName(order.getRecipientName())
                    .recipientPhone(order.getRecipientPhone())
                    .recipientEmail(order.getRecipientEmail())
                    .specialRequest(order.getSpecialRequest())
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
        @Schema(description = "가로 사이즈", example = "10.0")
        private double widthSize;

        @NotNull
        @Schema(description = "세로 사이즈", example = "10.0")
        private double lengthSize;

        @NotNull
        @Schema(description = "높이 사이즈", example = "10.0")
        private double heightSize;

        @NotNull
        @Schema(description = "배율", example = "1.0")
        private double magnification;

        @NotNull
        @Schema(description = "배송지", example = "서울시 강남구 역삼동 123-456 101호")
        private String deliveryAddress;

        @NotNull
        @Schema(description = "주문 수량", example = "1")
        private int quantity;

        private List<MultipartFile> modelFiles; // 여러 도면 파일 리스트

        @NotNull
        @Schema(description = "받는 사람 이름", example = "홍길동")
        private String recipientName;

        @NotNull
        @Schema(description = "받는 사람 전화번호", example = "010-1234-5678")
        private String recipientPhone;

        @NotNull
        @Schema(description = "받는 사람 이메일", example = "test@example.com")
        private String recipientEmail;

        @Schema(description = "요청 사항", example = "부재 시 경비실에 맡겨주세요.")
        private String specialRequest;
    }
}
