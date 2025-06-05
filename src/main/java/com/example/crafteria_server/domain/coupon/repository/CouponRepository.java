package com.example.crafteria_server.domain.coupon.repository;

import com.example.crafteria_server.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    List<Coupon> findByUserIdAndUsedFalseAndExpiredAtAfter(Long userId, LocalDateTime now);

    List<Coupon> findByUserId(Long userId);

    int countByUserIdAndTemplateId(Long userId, Long templateId);
}
