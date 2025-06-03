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
        private Long orderId;

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
        private Amount amount;
        private String paymentMethod;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Amount {
            private BigDecimal total;
        }
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

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortOneResponseWrapper {
        private int code;
        private String message;
        private PaymentResponse response;
    }
}
