package com.example.crafteria_server.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    @NotBlank(message = "이름은 필수 입력 값입니다.")
    private String username;

    @NotBlank(message = "실명은 필수 입력 값입니다.")
    private String realname;

    @NotBlank(message = "주소는 필수 입력 값입니다.")
    private String address;
}
