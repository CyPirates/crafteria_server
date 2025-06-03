package com.example.crafteria_server.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.bytebuddy.implementation.bind.annotation.Super;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class DashboardPendingUserDto {

    @Schema(description = "사용자 ID", example = "1")
    private Long id;

    @Schema(description = "사용자 이름", example = "john_doe")
    private String username;

    @Schema(description = "사용자 실명", example = "John Doe")
    private String realname;

    @Schema(description = "제조사 이름", example = "Example Manufacturer")
    private String manufacturerName;

    @Schema(description = "제조사 설명", example = "This is an example manufacturer description.")
    private String manufacturerDescription;

    @Schema(description = "제조사 주소", example = "123 Example Street, Example City, EX 12345")
    private String address; // 주소

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phoneNumber; // 전화번호
}
