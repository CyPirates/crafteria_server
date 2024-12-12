package com.example.crafteria_server.domain.user.service;

import com.example.crafteria_server.domain.user.entity.Role;
import com.example.crafteria_server.domain.user.entity.User;
import com.example.crafteria_server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserRoleService {
    private final UserRepository userRepository;

    // 역할 변경 메서드
    @Transactional
    public void updateRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (newRole == Role.DASHBOARD) {
            user.setDashboardStatus("PENDING"); // DASHBOARD로 변경 시 기본 상태는 PENDING
        } else {
            user.setDashboardStatus(null); // USER로 변경 시 상태 초기화
        }

        user.setRole(newRole);
        userRepository.save(user);
    }

    // DASHBOARD 사용자의 상태를 APPROVED로 변경
    @Transactional
    public void approveDashboard(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == Role.DASHBOARD && "PENDING".equals(user.getDashboardStatus())) {
            user.setDashboardStatus("APPROVED");
            userRepository.save(user);
        } else {
            throw new RuntimeException("Invalid user or status");
        }
    }
}
