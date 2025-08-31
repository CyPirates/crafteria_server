package com.example.crafteria_server.domain.identity.controller;

import com.example.crafteria_server.domain.identity.service.IdentityVerificationService;
import com.example.crafteria_server.global.portone.PortOneClient;
import com.example.crafteria_server.global.response.JsonBody;
import com.example.crafteria_server.global.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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
public class IdentityVerificationController {

    private final IdentityVerificationService service;

    /** ✅ 플로우 #1: 브라우저 SDK → 서버 조회·반영 */
    @PostMapping("/verify")
    @Operation(summary="본인인증 결과 조회/반영 (프론트 SDK 완료 후)")
    public JsonBody<Map<String,Object>> verify(@AuthenticationPrincipal PrincipalDetails principal,
                                               @RequestBody Map<String,String> body) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        String id = Optional.ofNullable(body.get("identityVerificationId"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "identityVerificationId 필수"));
        var v = service.fetchVerified(id);
        service.attachVerificationToUser(principal.getUserId(), id, v);

        return JsonBody.of(200, "성공", Map.of(
                "status", "VERIFIED",
                "name", v.getName(),
                "phoneNumber", maskPhone(v.getPhoneNumber()),
                "verifiedAt", v.getVerifiedAt()
        ));
    }

    /** ✅ 플로우 #2-1: 서버발송 send */
    @PostMapping("/send")
    @Operation(summary="서버발송: 인증요청 전송")
    public JsonBody<String> send(@AuthenticationPrincipal PrincipalDetails principal,
                                 @RequestParam @NonNull String identityVerificationId,
                                 @RequestBody PortOneClient.SendBody body) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        service.send(identityVerificationId, body);
        return JsonBody.of(200, "전송 완료", identityVerificationId);
    }

    /** ✅ 플로우 #2-2: 서버발송 confirm → 반영 */
    @PostMapping("/{id}/confirm")
    @Operation(summary="서버발송: OTP 확인/반영")
    public JsonBody<Map<String,Object>> confirm(@AuthenticationPrincipal PrincipalDetails principal,
                                                @PathVariable("id") String id,
                                                @RequestParam String otp) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        var v = service.confirm(id, otp);
        service.attachVerificationToUser(principal.getUserId(), id, v);

        return JsonBody.of(200, "확인 완료", Map.of(
                "status", "VERIFIED",
                "name", v.getName(),
                "phoneNumber", maskPhone(v.getPhoneNumber()),
                "verifiedAt", v.getVerifiedAt()
        ));
    }

    private String maskPhone(String p) {
        if (p == null) return null;
        String d = p.replaceAll("\\D", "");
        if (d.length() < 7) return "***";
        return d.substring(0,3) + "-****-" + d.substring(d.length()-4);
    }
}
