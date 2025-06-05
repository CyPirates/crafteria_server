package com.example.crafteria_server.domain.coupon.controller;

import com.example.crafteria_server.domain.coupon.dto.CouponTemplateDto;
import com.example.crafteria_server.domain.coupon.service.CouponTemplateService;
import com.example.crafteria_server.global.response.JsonBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j(topic = "AdminCouponTemplateController")
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/coupon-templates")
@Tag(name = "관리자 쿠폰 템플릿", description = "관리자가 사용하는 쿠폰 템플릿 API")
public class AdminCouponTemplateController {

    private final CouponTemplateService couponTemplateService;

    @PostMapping
    @Operation(summary = "쿠폰 템플릿 생성", description = "쿠폰 템플릿(코드 기반)을 생성합니다.")
    public ResponseEntity<JsonBody<CouponTemplateDto.Response>> createTemplate(
            @RequestBody CouponTemplateDto.CreateRequest request) {

        CouponTemplateDto.Response response = couponTemplateService.createTemplate(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(JsonBody.of(201, "쿠폰 템플릿 생성 성공", response));
    }

    @GetMapping
    @Operation(summary = "쿠폰 템플릿 전체 조회", description = "모든 쿠폰 템플릿을 조회합니다.")
    public ResponseEntity<JsonBody<List<CouponTemplateDto.Response>>> getAllTemplates() {
        List<CouponTemplateDto.Response> responseList = couponTemplateService.getAllTemplates();
        return ResponseEntity
                .ok(JsonBody.of(200, "쿠폰 템플릿 조회 성공", responseList));
    }
}
