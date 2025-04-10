package com.example.crafteria_server.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    @NotNull
    @Schema(description = "아이디", example = "dashboard_user")
    private String username;

    @NotNull
    @Schema(description = "비밀번호", example = "password123")
    private String password;

    @NotNull
    @Schema(description = "실명", example = "홍길동")
    private String realname;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phoneNumber;

    @Schema(description = "주소", example = "서울시 강남구")
    private String address;
}
