package com.example.crafteria_server.domain.model.dto;

import com.example.crafteria_server.domain.model.entity.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

public class UserModelDto {
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        @NotNull
        @Schema(description = "모델 ID", example = "1")
        private Long id;

        @NotNull
        @Schema(description = "모델 이름", example = "모델 이름")
        private String name;

        @Schema(description = "모델 설명", example = "모델 설명")
        private String description;

        @NotNull
        @Schema(description = "모델 평점", example = "5")
        private int rating;

        @NotNull
        @Schema(description = "모델 가격", example = "5000")
        private long price;

        @NotNull
        @Schema(description = "모델 조회수", example = "100")
        private long viewCount;

        @NotNull
        @Schema(description = "모델 다운로드수", example = "50")
        private long downloadCount;

        @NotNull
        @Schema(description = "모델 최소 사이즈", example = "10")
        private int minimumSize;

        @NotNull
        @Schema(description = "모델 최대 사이즈", example = "100")
        private int maximumSize;

        @NotNull
        @Schema(description = "모델 파일 URL", example = "http://localhost:8080/model/1")
        private String modelFileUrl;

        public static Response from(Model model) {
            return Response.builder()
                    .id(model.getId())
                    .name(model.getName())
                    .description(model.getDescription())
                    .rating(model.getRating())
                    .price(model.getPrice())
                    .viewCount(model.getViewCount())
                    .downloadCount(model.getDownloadCount())
                    .minimumSize(model.getMinimumSize())
                    .maximumSize(model.getMaximumSize())
                    .modelFileUrl(model.getModelFile().getUrl())
                    .build();
        }
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "모델 등록 요청", description = "모델 등록 요청", type = "multipartForm")
    public static class UploadRequest {
        @NotNull
        @Schema(description = "모델 이름", example = "모델 이름")
        private String name;

        @Schema(description = "모델 설명", example = "모델 설명")
        private String description;

        @NotNull
        @Schema(description = "모델 가격", example = "5000")
        private long price;

        @NotNull
        @Schema(description = "모델 최소 사이즈", example = "10")
        private int minimumSize;

        @NotNull
        @Schema(description = "모델 최대 사이즈", example = "100")
        private int maximumSize;

        @Schema(description = "모델 파일", format = "binary")
        private MultipartFile modelFile;
    }
}
