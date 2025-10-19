package com.example.crafteria_server.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserBasicInfoUpdateRequest {

    @NotBlank(message = "닉네임은 필수입니다.")
    private String username;

    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^01[016789]-\\d{3,4}-\\d{4}$", message = "유효한 전화번호 형식이 아닙니다. 예: 010-1234-5678")
    private String phoneNumber;
}
