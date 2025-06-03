package com.example.crafteria_server.domain.user.controller;

import com.example.crafteria_server.domain.user.dto.LoginDto;
import com.example.crafteria_server.domain.user.dto.RegisterRequest;
import com.example.crafteria_server.domain.user.service.UserService;
import com.example.crafteria_server.global.response.JsonBody;
import com.example.crafteria_server.global.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j(topic = "AuthController")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "대시보드 사용자 회원가입", description = "대시보드 사용자를 등록합니다.")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        log.info("회원가입 요청 - 이름: {}, 생성일: {}", request.getRealname(), LocalDateTime.now());
        userService.registerDashboardUser(request);
        return ResponseEntity.ok("회원가입 성공. 대시보드 계정으로 등록되었습니다.");
    }

    @PostMapping("/login")
    @Operation(summary = "대시보드 사용자 로그인", description = "대시보드 사용자로 로그인합니다.")
    public ResponseEntity<JsonBody<LoginDto.LoginResponse>> login(@RequestBody @Valid LoginDto.LoginRequest request) {
        LoginDto.LoginResponse response = userService.login(request);
        log.info("로그인 요청 - 이름: {}, 로그인 시간: {}", request.getUsername(), LocalDateTime.now());
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

    @PostMapping("/check-username")
    @Operation(summary = "아이디 중복 체크", description = "입력된 아이디가 이미 사용 중인지 확인합니다.")
    public ResponseEntity<JsonBody<Boolean>> checkUsernameAvailability(@RequestParam String username) {
        boolean isAvailable = userService.checkUsernameAvailability(username);
        return ResponseEntity.ok(JsonBody.of(200, "아이디 중복 검사 결과", isAvailable));
    }
}
