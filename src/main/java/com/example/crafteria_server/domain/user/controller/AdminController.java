package com.example.crafteria_server.domain.user.controller;

import com.example.crafteria_server.domain.user.entity.DashboardStatus;
import com.example.crafteria_server.domain.user.entity.User;
import com.example.crafteria_server.domain.user.repository.UserRepository;
import com.example.crafteria_server.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j(topic = "AdminController")
public class AdminController {
    private final UserService userService;
    private final UserRepository userRepository;

    // 역할 변경 API
    @PatchMapping("/dashboard/{userId}/approve")
    @Operation(summary = "대시보드 유저 승인", description = "대기 상태의 대시보드 유저를 승인합니다.")
    public ResponseEntity<?> approveDashboardUser(@PathVariable Long userId) {
        userService.updateDashboardStatus(userId, DashboardStatus.APPROVED);
        return ResponseEntity.ok("유저가 승인되었습니다.");
    }

    @PatchMapping("/dashboard/{userId}/reject")
    @Operation(summary = "대시보드 유저 거절", description = "대기 상태의 대시보드 유저를 거절합니다.")
    public ResponseEntity<?> rejectDashboardUser(@PathVariable Long userId) {
        userService.updateDashboardStatus(userId, DashboardStatus.REJECTED);
        return ResponseEntity.ok("유저가 거절되었습니다.");
    }

    @GetMapping("/dashboard/pending")
    @Operation(summary = "승인 대기 중인 대시보드 유저 조회", description = "대기 상태의 대시보드 유저를 조회합니다.")
    public ResponseEntity<?> getPendingDashboardUsers() {
        List<User> pendingUsers = userRepository.findByDashboardStatus(DashboardStatus.PENDING);
        return ResponseEntity.ok(pendingUsers.stream()
                .map(user -> Map.of("id", user.getId(), "username", user.getUsername(), "realname", user.getRealname()))
                .toList());
    }
}


