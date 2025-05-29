package com.example.crafteria_server.domain.user.controller;

import com.example.crafteria_server.domain.user.dto.DashboardPendingUserDto;
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
        log.info("대시보드 사용자 승인 요청 - 대상 유저ID: {}", userId);
        userService.updateDashboardStatus(userId, DashboardStatus.APPROVED);
        return ResponseEntity.ok("유저가 승인되었습니다.");
    }


    @PatchMapping("/dashboard/{userId}/reject")
    @Operation(summary = "대시보드 사용자 거절", description = "대시보드 사용자를 거절합니다.")
    public ResponseEntity<?> rejectDashboardUser(@PathVariable Long userId) {
        log.info("대시보드 사용자 거절 요청 - 대상 유저ID: {}", userId);
        userService.updateDashboardStatus(userId, DashboardStatus.REJECTED);
        return ResponseEntity.ok("유저가 거절되었습니다.");
    }

    @GetMapping("/dashboard/pending")
    public ResponseEntity<?> getPendingDashboardUsers() {
        List<User> pendingUsers = userRepository.findByDashboardStatus(DashboardStatus.PENDING);
        List<DashboardPendingUserDto> result = pendingUsers.stream()
                .map(user -> new DashboardPendingUserDto(
                        user.getId(),
                        user.getUsername(),
                        user.getRealname(),
                        user.getManufacturerName(),
                        user.getManufacturerDescription()
                ))
                .toList();

        return ResponseEntity.ok(result);
    }
}


