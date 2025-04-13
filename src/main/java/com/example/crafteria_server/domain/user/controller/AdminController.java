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

import java.util.HashMap;
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
    @Operation(summary = "대시보드 사용자 승인", description = "대시보드 사용자를 승인합니다.")
    public ResponseEntity<?> approveDashboardUser(@PathVariable Long userId) {
        userService.updateDashboardStatus(userId, DashboardStatus.APPROVED);
        return ResponseEntity.ok("유저가 승인되었습니다.");
    }

    @PatchMapping("/dashboard/{userId}/reject")
    @Operation(summary = "대시보드 사용자 거절", description = "대시보드 사용자를 거절합니다.")
    public ResponseEntity<?> rejectDashboardUser(@PathVariable Long userId) {
        userService.updateDashboardStatus(userId, DashboardStatus.REJECTED);
        return ResponseEntity.ok("유저가 거절되었습니다.");
    }

    @GetMapping("/dashboard/pending")
    @Operation(summary = "대시보드 대기 상태 조회", description = "대시보드 대기 상태 목록을 조회합니다.")
    public ResponseEntity<?> getPendingDashboardUsers() {
        List<User> pendingUsers = userRepository.findByDashboardStatus(DashboardStatus.PENDING);
        List<Map<String, Object>> result = pendingUsers.stream()
                .map(user -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", user.getId());
                    map.put("username", user.getUsername());
                    map.put("realname", user.getRealname());
                    map.put("manufacturerName", user.getManufacturerName()); // null 허용됨
                    map.put("manufacturerDescription", user.getManufacturerDescription());
                    return map;
                })
                .toList();
        return ResponseEntity.ok(result);
    }
}


