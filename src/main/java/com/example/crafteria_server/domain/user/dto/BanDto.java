package com.example.crafteria_server.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class BanDto {
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BanRequest {
        @NotNull(message = "밴 종료 날짜는 필수입니다.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @Schema(description = "밴 종료 날짜", example = "2021-08-01T00:00:00")
        private LocalDateTime banUntil;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BanResponse {
        @Schema(description = "유저 ID", example = "1")
        private Long userId;

        @Schema(description = "밴 종료 날짜", example = "2021-08-01T00:00:00")
        private LocalDateTime banUntil;

        @Schema(description = "밴 상태", example = "true")
        private String statusMessage;
    }
}
