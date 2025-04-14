package com.example.crafteria_server.domain.pay.dto;

import lombok.*;

import java.math.BigDecimal;

public class PaymentDto {
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentRequestDto {
        private String paymentId;
        private Long order;

    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentResultDto {
        private String status;
        private String message;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentResponse {
        private String status;
        private BigDecimal amount;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModelPaymentRequestDto {
        private String paymentId;
        private Long modelId;
    }
}
