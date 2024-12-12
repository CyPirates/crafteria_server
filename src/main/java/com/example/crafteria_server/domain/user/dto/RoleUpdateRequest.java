package com.example.crafteria_server.domain.user.dto;

import com.example.crafteria_server.domain.user.entity.Role;
import lombok.Getter;
import lombok.Setter;

// 역할 변경 요청 DTO
@Getter
@Setter
public class RoleUpdateRequest {
    private Role newRole; // USER 또는 DASHBOARD
}