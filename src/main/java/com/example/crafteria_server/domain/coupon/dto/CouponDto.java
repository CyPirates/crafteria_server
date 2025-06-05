package com.example.crafteria_server.domain.coupon.dto;

import com.example.crafteria_server.domain.coupon.entity.CouponType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import lombok.*;

import java.time.LocalDateTime;

public class CouponDto {
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @Schema(description = "쿠폰 이름", example = "10% 할인 쿠폰")
        private String name;

        @Schema(description = "쿠폰 코드", example = "COUPON2023")
        private String code;

        @Schema(description = "할인 퍼센트", example = "10")
        private int discountRate;

        @Schema(description = "최대 할인 금액", example = "50000")
        private int maxDiscountAmount;

        @Schema(description = "발급일", example = "2023-10-01T10:00:00")
        private LocalDateTime expiredAt;

        @Schema(description = "쿠폰 타입", example = "MODEL_PURCHASE")
        private CouponType type; // <== 추가
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        @Schema(description = "쿠폰 ID", example = "1")
        private Long id;

        @Schema(description = "쿠폰 이름", example = "10% 할인 쿠폰")
        private String name;

        @Schema(description = "쿠폰 코드", example = "COUPON2023")
        private String code;

        @Schema(description = "할인 퍼센트", example = "10%")
        private int discountRate;

        @Schema(description = "최대 할인 금액", example = "50000")
        private int maxDiscountAmount;

        @Schema(description = "발급일", example = "2023-10-01T10:00:00")
        private LocalDateTime issuedAt;

        @Schema(description = "만료일", example = "2023-12-31T23:59:59")
        private LocalDateTime expiredAt;

        @Schema(description = "쿠폰 사용 여부", example = "false")
        private boolean used;

        @Schema(description = "쿠폰 타입", example = "MODEL_PURCHASE")
        private CouponType type; // <== 추가
    }

}
