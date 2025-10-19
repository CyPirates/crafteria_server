package com.example.crafteria_server.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBankAccountDto {
    @Schema(description = "계좌번호(공백/하이픈 허용, 서버에서 정규화)", example = "110-123-456789")
    @NotBlank
    private String accountNumber;

    @Schema(description = "계좌 종류 (은행명 등)", example = "국민은행")
    private String accountType;

}

