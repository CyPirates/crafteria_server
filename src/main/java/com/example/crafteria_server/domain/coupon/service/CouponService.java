package com.example.crafteria_server.domain.coupon.service;

import com.example.crafteria_server.domain.coupon.dto.CouponDto;
import com.example.crafteria_server.domain.coupon.entity.Coupon;
import com.example.crafteria_server.domain.coupon.repository.CouponRepository;
import com.example.crafteria_server.domain.user.entity.User;
import com.example.crafteria_server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    // 생성
    public CouponDto.Response createCoupon(CouponDto.CreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        Coupon coupon = Coupon.builder()
                .user(user)
                .name(request.getName())
                .code(request.getCode())
                .discountRate(request.getDiscountRate())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .issuedAt(LocalDateTime.now())
                .expiredAt(request.getExpiredAt())
                .used(false)
                .type(request.getType())
                .build();

        return toDto(couponRepository.save(coupon));
    }

    // 삭제
    public void deleteCoupon(Long couponId) {
        couponRepository.deleteById(couponId);
    }

    // 사용 가능한 쿠폰 조회
    public List<CouponDto.Response> getAvailableCoupons(Long userId) {
        return couponRepository.findByUserIdAndUsedFalseAndExpiredAtAfter(userId, LocalDateTime.now())
                .stream().map(this::toDto)
                .collect(Collectors.toList());
    }

    // 전체 쿠폰 조회
    public List<CouponDto.Response> getAllCoupons(Long userId) {
        return couponRepository.findByUserId(userId)
                .stream().map(this::toDto)
                .collect(Collectors.toList());
    }

    private CouponDto.Response toDto(Coupon coupon) {
        return CouponDto.Response.builder()
                .id(coupon.getId())
                .name(coupon.getName())
                .code(coupon.getCode())
                .discountRate(coupon.getDiscountRate())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .issuedAt(coupon.getIssuedAt())
                .expiredAt(coupon.getExpiredAt())
                .used(coupon.isUsed())
                .build();
    }
}
