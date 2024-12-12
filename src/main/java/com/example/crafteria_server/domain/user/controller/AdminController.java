package com.example.crafteria_server.domain.user.controller;

import com.example.crafteria_server.domain.user.dto.RoleUpdateRequest;
import com.example.crafteria_server.domain.user.entity.Role;
import com.example.crafteria_server.domain.user.service.UserRoleService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j(topic = "AdminController")
public class AdminController {
    private final UserRoleService userRoleService;

    // 역할 변경 API
    @PatchMapping("/users/{userId}/role")
    @Operation(summary = "사용자 역할 변경")
    public ResponseEntity<?> updateUserRole(@PathVariable Long userId, @RequestBody RoleUpdateRequest request) {
        userRoleService.updateRole(userId, request.getNewRole());
        return ResponseEntity.ok("User role updated successfully");
    }

    // DASHBOARD 사용자 승인 API
    @PatchMapping("/dashboard/{userId}/approve")
    @Operation(summary = "Dashboard 사용자 승인")
    public ResponseEntity<?> approveDashboardUser(@PathVariable Long userId) {
        userRoleService.approveDashboard(userId);
        return ResponseEntity.ok("Dashboard user approved");
    }
}


