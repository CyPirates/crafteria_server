package com.example.crafteria_server.domain.user.controller;

import com.example.crafteria_server.domain.user.dto.LoginDto;
import com.example.crafteria_server.domain.user.dto.RegisterRequest;
import com.example.crafteria_server.domain.user.service.UserService;
import com.example.crafteria_server.global.response.JsonBody;
import com.example.crafteria_server.global.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "일반 회원가입", description = "DASHBOARD 역할로 일반 회원가입을 진행합니다.")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        userService.registerDashboardUser(request);
        return ResponseEntity.ok("회원가입 성공. 대시보드 계정으로 등록되었습니다.");
    }

    @PostMapping("/login")
    @Operation(summary = "일반 로그인", description = "아이디와 비밀번호로 로그인합니다.")
    public ResponseEntity<JsonBody<LoginDto.LoginResponse>> login(@RequestBody @Valid LoginDto.LoginRequest request) {
        LoginDto.LoginResponse response = userService.login(request);
        return ResponseEntity.ok(JsonBody.of(200, "로그인 성공", response));
    }

    @GetMapping("/manufacturer-id")
    @Operation(summary = "로그인된 대시보드 사용자의 제조사 ID 조회", description = "로그인된 대시보드 사용자의 연결된 제조사 ID를 반환합니다.")
    public ResponseEntity<?> getLoggedInUserManufacturerId(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (principalDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("사용자 인증 정보가 없습니다.");
        }

        String manufacturerId = userService.getLoggedInUserManufacturerId(principalDetails);
        return ResponseEntity.ok(manufacturerId);
    }
}
