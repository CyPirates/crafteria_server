package com.example.crafteria_server.domain.user.service;

import com.example.crafteria_server.domain.user.entity.Role;
import com.example.crafteria_server.domain.user.entity.User;
import com.example.crafteria_server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardApprovalService {
    private final UserRepository userRepository;

    public void approveDashboardUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (Role.DASHBOARD.equals(user.getRole()) && "PENDING".equals(user.getDashboardStatus())) {
            user.setDashboardStatus("APPROVED");
            userRepository.save(user);
        } else {
            throw new RuntimeException("Invalid user or state");
        }
    }
}
