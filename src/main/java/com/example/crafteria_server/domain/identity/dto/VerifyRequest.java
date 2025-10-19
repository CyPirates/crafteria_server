package com.example.crafteria_server.domain.identity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyRequest {
    @Schema(description = "포트원 본인인증 ID", example = "iv-12345678-1234-1234-1234-1234567890ab", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String identityVerificationId;
}
