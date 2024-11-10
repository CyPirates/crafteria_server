package com.example.crafteria_server.domain.review.dto;

import lombok.*;

import java.time.LocalDateTime;

public class ReviewDto {
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewRequestDto {
        private Long manufacturerId;
        private String content;
        private int rating;
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
    }
}
