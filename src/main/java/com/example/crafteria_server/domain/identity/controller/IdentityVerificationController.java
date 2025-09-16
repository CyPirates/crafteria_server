package com.example.crafteria_server.domain.identity.controller;

import com.example.crafteria_server.domain.identity.dto.VerifiedIdentityUnified;
import com.example.crafteria_server.domain.identity.dto.VerifyRequest;
import com.example.crafteria_server.domain.identity.dto.VerifyResponse;
import com.example.crafteria_server.domain.identity.service.IdentityVerificationService;
import com.example.crafteria_server.global.portone.PortOneClient;
import com.example.crafteria_server.global.portone.PortOneProps;
import com.example.crafteria_server.global.response.JsonBody;
import com.example.crafteria_server.global.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/identity-verifications")
@RequiredArgsConstructor
@Tag(name = "본인인증 API")
@Slf4j(topic = "IdentityVerificationController")
public class IdentityVerificationController {

    private final IdentityVerificationService service;
    private final PortOneClient portOneClient;
    private final PortOneProps portOneProps;

    /** ✅ 플로우 #1: 브라우저 SDK → 서버 조회·반영 */
    @PostMapping("/verify")
    @Operation(summary="본인인증 결과 조회/반영 (프론트 SDK 완료 후)")
    public JsonBody<VerifyResponse> verify(@AuthenticationPrincipal PrincipalDetails principal,
                                           @RequestBody @Valid VerifyRequest req) {

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        String id = req.getIdentityVerificationId();

        // (디버깅) RAW 한 번만 찍고 나중에 주석 처리해도 됨
        String raw = portOneClient.getRaw(id, portOneProps.getStoreId()).block();
        log.info("[PortOne RAW id={}] {}", id, raw);

        VerifiedIdentityUnified v = service.fetchVerified(id);
        service.attachVerificationToUser(principal.getUserId(), id, v);

        // 응답은 null-safe DTO 사용
        VerifyResponse resp = new VerifyResponse();
        resp.setStatus("VERIFIED");
        resp.setName(v.getName());
        if (v.getPhoneNumber() != null) {
            resp.setPhoneNumber(maskPhone(v.getPhoneNumber()));
        }
        resp.setVerifiedAt(v.getVerifiedAt());

        return JsonBody.of(200, "성공", resp);
    }


    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

}
