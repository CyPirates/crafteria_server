package com.example.crafteria_server.domain.coupon.entity;

import com.example.crafteria_server.domain.user.entity.User;
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
@Table(name = "coupon")
public class Coupon extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private int discountRate;

    @Column(nullable = false)
    private int maxDiscountAmount;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    @Column(nullable = false)
    private boolean used;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponType type;  // <== 추가

    @Column(nullable = false)
    private int maxPerUser; // 유저당 발급 가능 횟수


}
