package com.example.crafteria_server.domain.coupon.repository;

import com.example.crafteria_server.domain.coupon.entity.CouponTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponTemplateRepository extends JpaRepository<CouponTemplate, Long> {
    Optional<CouponTemplate> findByCode(String code);
}

