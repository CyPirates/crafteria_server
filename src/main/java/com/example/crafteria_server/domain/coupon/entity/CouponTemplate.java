package com.example.crafteria_server.domain.coupon.entity;

import com.example.crafteria_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "coupon_template")
public class CouponTemplate extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code; // 사용자가 입력할 쿠폰 코드

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponType type;

    @Column(nullable = false)
    private int discountRate;

    @Column(nullable = false)
    private int maxDiscountAmount;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    @Column(nullable = false)
    private int maxPerUser; // 유저당 발급 가능 횟수

    @Column(nullable = false)
    private int maxTotalIssueCount; // 총 발급 가능 수량 (예: 100)

    @Column(nullable = false)
    private int currentIssueCount;  // 현재 발급된 수량
}
