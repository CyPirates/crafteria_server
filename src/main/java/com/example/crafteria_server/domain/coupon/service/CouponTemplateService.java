package com.example.crafteria_server.domain.coupon.service;

import com.example.crafteria_server.domain.coupon.dto.CouponTemplateDto;
import com.example.crafteria_server.domain.coupon.entity.CouponTemplate;
import com.example.crafteria_server.domain.coupon.repository.CouponTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "CouponTemplateService")
public class CouponTemplateService {

    private final CouponTemplateRepository couponTemplateRepository;

    public CouponTemplateDto.Response createTemplate(CouponTemplateDto.CreateRequest request) {
        if (couponTemplateRepository.findByCode(request.getCode()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 쿠폰 코드입니다.");
        }

        CouponTemplate template = CouponTemplate.builder()
                .code(request.getCode())
                .name(request.getName())
                .type(request.getType())
                .discountRate(request.getDiscountRate())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .expiredAt(request.getExpiredAt())
                .maxPerUser(request.getMaxPerUser())
                .maxTotalIssueCount(request.getMaxTotalIssueCount())
                .currentIssueCount(0)
                .build();

        return toDto(couponTemplateRepository.save(template));
    }

    public List<CouponTemplateDto.Response> getAllTemplates() {
        return couponTemplateRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private CouponTemplateDto.Response toDto(CouponTemplate template) {
        return CouponTemplateDto.Response.builder()
                .id(template.getId())
                .code(template.getCode())
                .name(template.getName())
                .type(template.getType())
                .discountRate(template.getDiscountRate())
                .maxDiscountAmount(template.getMaxDiscountAmount())
                .expiredAt(template.getExpiredAt())
                .maxPerUser(template.getMaxPerUser())
                .maxTotalIssueCount(template.getMaxTotalIssueCount())
                .currentIssueCount(template.getCurrentIssueCount())
                .createDate(template.getCreateDate())
                .updateDate(template.getUpdateDate())
                .build();
    }
}
