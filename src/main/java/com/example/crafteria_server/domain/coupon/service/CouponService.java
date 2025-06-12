package com.example.crafteria_server.domain.coupon.service;

import com.example.crafteria_server.domain.coupon.dto.CouponDto;
import com.example.crafteria_server.domain.coupon.entity.Coupon;
import com.example.crafteria_server.domain.coupon.entity.CouponTemplate;
import com.example.crafteria_server.domain.coupon.entity.CouponType;
import com.example.crafteria_server.domain.coupon.repository.CouponRepository;
import com.example.crafteria_server.domain.coupon.repository.CouponTemplateRepository;
import com.example.crafteria_server.domain.user.entity.User;
import com.example.crafteria_server.domain.user.repository.UserRepository;
import com.example.crafteria_server.global.security.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final CouponTemplateRepository couponTemplateRepository;

    // 사용 가능한 쿠폰 조회
    public List<CouponDto.Response> getAvailableCoupons(Long userId) {
        return couponRepository.findByUserIdAndUsedFalseAndExpiredAtAfter(userId, LocalDateTime.now())
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // 전체 쿠폰 조회
    public List<CouponDto.Response> getAllCoupons(Long userId) {
        return couponRepository.findByUserId(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // 쿠폰 발급 (코드 입력 방식)
    public CouponDto.Response issueCouponByCode(String code, Long userId) {
        CouponTemplate template = couponTemplateRepository.findByCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 쿠폰 코드는 존재하지 않습니다."));

        if (template.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "쿠폰 코드가 만료되었습니다.");
        }

        if (template.getCurrentIssueCount() >= template.getMaxTotalIssueCount()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "쿠폰이 모두 소진되었습니다.");
        }

        int userCouponCount = couponRepository.countByUserIdAndTemplateId(userId, template.getId());
        if (userCouponCount >= template.getMaxPerUser()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "해당 쿠폰은 유저당 최대 횟수만큼 발급받았습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        Coupon coupon = Coupon.builder()
                .user(user)
                .template(template)
                .name(template.getName())
                .code(template.getCode())
                .discountRate(template.getDiscountRate())
                .maxDiscountAmount(template.getMaxDiscountAmount())
                .issuedAt(LocalDateTime.now())
                .expiredAt(template.getExpiredAt())
                .used(false)
                .type(template.getType())
                .build();

        couponRepository.save(coupon);

        template.setCurrentIssueCount(template.getCurrentIssueCount() + 1);
        couponTemplateRepository.save(template);

        return toDto(coupon);
    }

    // 도면 쿠폰 유효성 검증
    public Coupon validateModelCoupon(Long couponId, Long userId) {
        return validateCouponInternal(couponId, userId, CouponType.MODEL_PURCHASE);
    }

    // 주문 쿠폰 유효성 검증
    public Coupon validateOrderCoupon(Long couponId, Long userId) {
        return validateCouponInternal(couponId, userId, CouponType.ORDER_PURCHASE);
    }

    // 공통 유효성 검증 메서드
    private Coupon validateCouponInternal(Long couponId, Long userId, CouponType requiredType) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));

        if (!coupon.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 쿠폰을 사용할 수 없습니다.");
        }

        if (coupon.isUsed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 사용된 쿠폰입니다.");
        }

        if (coupon.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "만료된 쿠폰입니다.");
        }

        if (coupon.getType() != requiredType) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "해당 쿠폰 타입이 올바르지 않습니다.");
        }

        return coupon;
    }

    public void markCouponAsUsed(Long couponId, Long userId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));

        if (!coupon.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 쿠폰을 사용할 권한이 없습니다.");
        }

        if (coupon.isUsed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 사용된 쿠폰입니다.");
        }

        coupon.setUsed(true);
        couponRepository.save(coupon);
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

    public void deleteCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));
        couponRepository.delete(coupon);
    }


}
