package com.example.crafteria_server.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
}
