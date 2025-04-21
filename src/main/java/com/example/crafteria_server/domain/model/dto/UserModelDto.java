package com.example.crafteria_server.domain.model.dto;

import com.example.crafteria_server.domain.model.entity.Model;
import com.example.crafteria_server.domain.model.entity.ModelCategory;
import com.example.crafteria_server.domain.model.entity.ModelPurchase;
import com.example.crafteria_server.domain.user.dto.AuthorDto;
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
    public static class ModelResponse {
        @NotNull
        @Schema(description = "모델 ID", example = "1")
        private Long id;

        @NotNull
        @Schema(description = "작가 dto", example = "작가 dto")
        private AuthorDto.AuthorResponse author;

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
        @Schema(description = "모델 가로 사이즈", example = "10")
        private double widthSize;

        @NotNull
        @Schema(description = "모델 세로 사이즈", example = "10")
        private double lengthSize;

        @NotNull
        @Schema(description = "모델 높이 사이즈", example = "100")
        private double heightSize;

        @NotNull
        @Schema(description = "모델 파일 URL", example = "http://localhost:8080/model/1")
        private String modelFileUrl;

        @NotNull
        @Schema(description = "구매 가능 여부", example = "true")
        private boolean purchaseAvailability;

        @NotNull
        @Schema(description = "모델 카테고리")
        private ModelCategory category;

        @Schema(description = "결제 ID", example = "a1b2c3d4-e5f6-7890-abcd-1234567890ef")
        private String paymentId;

        @NotNull
        @Schema(description = "도면 다운로드 가능 여부", example = "true")
        private boolean downloadable;


        public static ModelResponse from(Model model, boolean purchaseAvailability, boolean downloadable) {
            return ModelResponse.builder()
                    .id(model.getId())
                    .author(AuthorDto.AuthorResponse.from(model.getAuthor()))
                    .name(model.getName())
                    .description(model.getDescription())
                    .rating(model.getRating())
                    .price(model.getPrice())
                    .viewCount(model.getViewCount())
                    .downloadCount(model.getDownloadCount())
                    .widthSize(model.getWidthSize())
                    .lengthSize(model.getLengthSize())
                    .heightSize(model.getHeightSize())
                    .category(model.getCategory())
                    .purchaseAvailability(purchaseAvailability)
                    .modelFileUrl(model.getModelFile().getUrl())
                    .downloadable(downloadable)
                    .build();
        }

        public static ModelResponse from(ModelPurchase modelPurchase, boolean purchaseAvailability) {
            Model model = modelPurchase.getModel();
            return ModelResponse.builder()
                    .id(modelPurchase.getModel().getId())
                    .author(AuthorDto.AuthorResponse.from(modelPurchase.getModel().getAuthor()))
                    .name(modelPurchase.getModel().getName())
                    .description(modelPurchase.getModel().getDescription())
                    .rating(modelPurchase.getModel().getRating())
                    .price(modelPurchase.getModel().getPrice())
                    .viewCount(modelPurchase.getModel().getViewCount())
                    .downloadCount(modelPurchase.getModel().getDownloadCount())
                    .widthSize(modelPurchase.getModel().getWidthSize())
                    .lengthSize(modelPurchase.getModel().getLengthSize())
                    .heightSize(modelPurchase.getModel().getHeightSize())
                    .modelFileUrl(modelPurchase.getModel().getModelFile().getUrl())
                    .paymentId(modelPurchase.getPaymentId())
                    .downloadable(model.isDownloadable())
                    .build();
        }
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "모델 등록 요청", description = "모델 등록 요청", type = "multipartForm")
    public static class ModelUploadRequest {
        @NotNull
        @Schema(description = "모델 이름", example = "모델 이름")
        private String name;

        @Schema(description = "모델 설명", example = "모델 설명")
        private String description;

        @NotNull
        @Schema(description = "모델 가격", example = "5000")
        private long price;

        @NotNull
        @Schema(description = "모델 가로 사이즈", example = "10")
        private double widthSize;

        @NotNull
        @Schema(description = "모델 세로 사이즈", example = "100")
        private double lengthSize;

        @NotNull
        @Schema(description = "모델 높이 사이즈", example = "100")
        private double heightSize;

        @NotNull
        @Schema(description = "모델 파일", format = "binary")
        private MultipartFile modelFile;

        @NotNull
        @Schema(description = "모델 카테고리")
        private ModelCategory category;

        @Schema(description = "도면 다운로드 가능 여부", example = "true")
        private boolean downloadable;
    }
}
