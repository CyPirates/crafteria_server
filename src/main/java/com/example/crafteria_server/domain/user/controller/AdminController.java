package com.example.crafteria_server.domain.user.controller;

import com.example.crafteria_server.domain.user.service.DashboardApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final DashboardApprovalService dashboardApprovalService;

    @PostMapping("/dashboard/approve/{userId}")
    public ResponseEntity<?> approveDashboardUser(@PathVariable Long userId) {
        dashboardApprovalService.approveDashboardUser(userId);
        return ResponseEntity.ok("Dashboard user approved");
    }
}
