package com.example.crafteria_server.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public class ReviewDto {
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewRequestDto {
        @NotNull
        @Schema(description = "제조업체 ID", example = "1", required = true)
        private Long manufacturerId;

        @NotNull
        @Schema(description = "리뷰 내용", example = "아주 잘만들어졌습니다", required = true)
        private String content;

        @NotNull
        @Schema(description = "평점", example = "5", required = true)
        private int rating;

        private List<MultipartFile> imageFiles; // 선택적 첨부 이미지 파일 리스트
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewResponseDto {
        private Long id;
        private String content;
        private int rating;
        private LocalDateTime createdAt;
        private List<String> imageUrls;
    }
}
