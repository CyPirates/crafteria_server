package com.example.crafteria_server.domain.user.dto;

import com.example.crafteria_server.domain.user.entity.Role;
import com.example.crafteria_server.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String realname;
    private String oauth2Id;
    private Role role;
    private String dashboardStatus;

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRealname(),
                user.getOauth2Id(),
                user.getRole(),
                user.getDashboardStatus()
        );
    }
}
