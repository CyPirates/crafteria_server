package com.example.crafteria_server.domain.coupon.dto;

import com.example.crafteria_server.domain.coupon.entity.CouponType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

public class CouponTemplateDto {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {

        @Schema(description = "쿠폰 코드", example = "COUPON2023")
        private String code;

        @Schema(description = "쿠폰 이름", example = "10% 할인 쿠폰")
        private String name;

        @Schema(description = "쿠폰 타입", example = "MODEL_PURCHASE")
        private CouponType type;

        @Schema(description = "할인 퍼센트", example = "10")
        private int discountRate;

        @Schema(description = "최대 할인 금액", example = "50000")
        private int maxDiscountAmount;

        @Schema(description = "만료일", example = "2023-10-01T10:00:00")
        private LocalDateTime expiredAt;

        @Schema(description = "유저당 발급 가능 횟수", example = "5")
        private int maxPerUser;

        @Schema(description = "총 발급 가능 수량", example = "100")
        private int maxTotalIssueCount;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {

        @Schema(description = "쿠폰 템플릿 ID", example = "1")
        private Long id;

        @Schema(description = "쿠폰 코드", example = "COUPON2023")
        private String code;

        @Schema(description = "쿠폰 이름", example = "10% 할인 쿠폰")
        private String name;

        @Schema(description = "쿠폰 타입", example = "MODEL_PURCHASE")
        private CouponType type;

        @Schema(description = "할인 퍼센트", example = "10")
        private int discountRate;

        @Schema(description = "최대 할인 금액", example = "50000")
        private int maxDiscountAmount;

        @Schema(description = "만료일", example = "2023-10-01T10:00:00")
        private LocalDateTime expiredAt;

        @Schema(description = "유저당 발급 가능 횟수", example = "5")
        private int maxPerUser;

        @Schema(description = "총 발급 가능 수량", example = "100")
        private int maxTotalIssueCount;

        @Schema(description = "현재 발급된 수량", example = "10")
        private int currentIssueCount;

        @Schema(description = "생성일", example = "2023-10-01T10:00:00")
        private LocalDateTime createDate;

        @Schema(description = "수정일", example = "2023-10-01T10:00:00")
        private LocalDateTime updateDate;
    }
}
