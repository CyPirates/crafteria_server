package com.example.crafteria_server.domain.user.dto;

import com.example.crafteria_server.domain.user.entity.Author;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

public class AuthorDto {
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorResponse {
        @NotNull
        @Schema(description = "작가 ID = 유저 ID", example = "1")
        private Long id;

        @NotNull
        @Schema(description = "작가 이름" , example = "이찬호")
        private String name;

        @NotNull
        @Schema(description = "작가 평점", example = "5")
        private int rating;

        @Schema(description = "작가 소개", example = "작가 소개")
        private String introduction;

        @NotNull
        @Schema(description = "작가 모델 수", example = "10")
        private long modelCount;

        @NotNull
        @Schema(description = "작가 조회수", example = "100")
        private long viewCount;

        @Schema(description = "작가 프로필 이미지 URL", example = "http://localhost:8080/profile/1")
        private String profileImageUrl;

        public static AuthorResponse from(Author author) {
            String profileImageUrl = author.getProfileImage() == null ? null : author.getProfileImage().getUrl();
            return AuthorResponse.builder()
                    .id(author.getId())
                    .name(author.getRealname())
                    .rating(author.getRating())
                    .introduction(author.getIntroduction())
                    .modelCount(author.getModelCount())
                    .viewCount(author.getViewCount())
                    .profileImageUrl(profileImageUrl)
                    .build();
        }
    }
}
