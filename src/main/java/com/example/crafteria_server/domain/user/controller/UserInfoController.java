package com.example.crafteria_server.domain.user.controller;

import com.example.crafteria_server.domain.user.dto.UserResponse;
import com.example.crafteria_server.domain.user.entity.User;
import com.example.crafteria_server.domain.user.service.UserInfoService;
import com.example.crafteria_server.global.response.JsonBody;
import com.example.crafteria_server.global.security.PrincipalDetails;
import com.google.api.Authentication;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserInfoController {
    private final UserInfoService userInfoService;


    // 로그인한 사용자 정보 조회 API
    @GetMapping("/me")
    @Operation(summary = "현재 사용자 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    public JsonBody<UserResponse> getCurrentUser(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        // 인증되지 않은 사용자 처리
        if (principalDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        // 현재 로그인한 사용자 정보 가져오기
        User user = userInfoService.getCurrentUser(principalDetails.getUserId());
        return JsonBody.of(200, "성공", UserResponse.from(user));
    }

    // 특정 사용자 정보 조회 API
    @GetMapping("/{userId}")
    @Operation(summary = "특정 사용자 조회", description = "특정 사용자의 정보를 조회합니다.")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        User user = userInfoService.getUserById(userId);
        UserResponse response = UserResponse.from(user);
        return ResponseEntity.ok(response);
    }

    // 전체 사용자 정보 조회 API
    @GetMapping
    @Operation(summary = "모든 사용자 조회", description = "모든 사용자의 정보를 조회합니다.")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userInfoService.getAllUsers();
        List<UserResponse> responses = users.stream()
                .map(UserResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }
}
