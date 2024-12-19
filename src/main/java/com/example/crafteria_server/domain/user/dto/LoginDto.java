package com.example.crafteria_server.domain.user.dto;

import lombok.*;

public class LoginDto {
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class  LoginRequest {
        private String username;
        private String password;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoginResponse {
        private String username;
        private String role;
        private String accessToken;
    }
}
