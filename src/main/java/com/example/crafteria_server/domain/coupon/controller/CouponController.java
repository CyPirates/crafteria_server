package com.example.crafteria_server.domain.coupon.controller;

import com.example.crafteria_server.domain.coupon.dto.CouponDto;
import com.example.crafteria_server.domain.coupon.service.CouponService;
import com.example.crafteria_server.global.response.JsonBody;
import com.example.crafteria_server.global.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j(topic = "CouponController")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coupons")
public class CouponController {

    private final CouponService couponService;

    @DeleteMapping("/{id}")
    @Operation(summary = "쿠폰 삭제", description = "ID로 쿠폰을 삭제합니다.")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.noContent().build(); // 204 No Content + 본문 없음
    }
    
    @Operation(summary = "사용 가능한 쿠폰 조회", description = "현재 로그인한 유저의 유효한 쿠폰을 조회합니다.")
    @GetMapping("/available")
    public ResponseEntity<JsonBody<List<CouponDto.Response>>> getAvailableCoupons(
            @AuthenticationPrincipal PrincipalDetails principal) {
        Long userId = principal.getUserId();
        List<CouponDto.Response> responses = couponService.getAvailableCoupons(userId);
        return ResponseEntity.ok(JsonBody.of(200, "사용 가능한 쿠폰 조회 성공", responses));
    }

    @Operation(summary = "전체 쿠폰 조회", description = "현재 로그인한 유저의 전체 쿠폰을 조회합니다.")
    @GetMapping("/all")
    public ResponseEntity<JsonBody<List<CouponDto.Response>>> getAllCoupons(
            @AuthenticationPrincipal PrincipalDetails principal) {
        Long userId = principal.getUserId();
        List<CouponDto.Response> responses = couponService.getAllCoupons(userId);
        return ResponseEntity.ok(JsonBody.of(200, "전체 쿠폰 조회 성공", responses));
    }

    @Operation(summary = "쿠폰 발급 (코드 입력 방식)", description = "쿠폰 코드를 입력하면 발급됩니다. 발급 횟수 제한이 적용됩니다.")
    @PostMapping("/issue")
    public ResponseEntity<JsonBody<CouponDto.Response>> issueCouponByCode(
            @RequestParam String code,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        CouponDto.Response response = couponService.issueCouponByCode(code, principalDetails.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(JsonBody.of(201, "쿠폰 발급 성공", response));
    }


}
