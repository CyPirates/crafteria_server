package com.example.crafteria_server.domain.user.controller;

import com.example.crafteria_server.domain.user.dto.LoginDto;
import com.example.crafteria_server.domain.user.dto.RegisterRequest;
import com.example.crafteria_server.domain.user.service.UserService;
import com.example.crafteria_server.global.response.JsonBody;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
