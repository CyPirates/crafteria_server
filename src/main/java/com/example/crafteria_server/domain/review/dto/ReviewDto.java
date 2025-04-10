package com.example.crafteria_server.domain.review.dto;

import com.example.crafteria_server.domain.file.entity.File;
import com.example.crafteria_server.domain.review.entity.Review;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


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
        @Schema(description = "주문 ID", example = "123", required = true)
        private Long orderId;

        @NotNull
        @Schema(description = "리뷰 내용", example = "아주 잘만들어졌습니다", required = true)
        private String content;

        @NotNull
        @Schema(description = "평점", example = "5", required = true)
        private int rating;

        @Schema(description = "첨부 이미지 파일 리스트 (최대 3개)", type = "array",  nullable = true)
        private List<MultipartFile> imageFiles = new ArrayList<>();

    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewResponseDto {
        @Schema(description = "리뷰 ID", example = "1")
        private Long id;

        @Schema(description ="리뷰 내용", example = "아주 잘만들어졌습니다")
        private String content;

        @Schema(description = "평점", example = "5")
        private int rating;

        @Schema(description = "리뷰 작성일", example = "2021-08-01T00:00:00")
        private LocalDateTime createdAt;

        @Schema(description = "첨부 이미지 URL 리스트")
        private List<String> imageUrls;

        @Schema(description = "주문 ID", example = "1")
        private Long orderId;

        public static ReviewResponseDto from(Review review) {
            return ReviewResponseDto.builder()
                    .id(review.getId())
                    .content(review.getContent())
                    .rating(review.getRating())
                    .createdAt(review.getCreatedAt())
                    .imageUrls(review.getImages().stream().map(com.example.crafteria_server.domain.file.entity.File::getUrl).collect(Collectors.toList()))
                    .orderId(review.getOrder().getId())  // 주문 ID 매핑
                    .build();
        }
    }
}
