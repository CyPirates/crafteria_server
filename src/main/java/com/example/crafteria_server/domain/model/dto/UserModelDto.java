package com.example.crafteria_server.domain.model.dto;

import com.example.crafteria_server.domain.file.entity.File;
import com.example.crafteria_server.domain.model.entity.Model;
import com.example.crafteria_server.domain.model.entity.ModelCategory;
import com.example.crafteria_server.domain.model.entity.ModelPurchase;
import com.example.crafteria_server.domain.user.dto.AuthorDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

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

        @Schema(description = "STL 파일 URL 리스트")
        private List<String> modelFileUrls;

        @Schema(description = "설명용 이미지 URL 리스트")
        private java.util.List<String> descriptionImageUrls;

        @Schema(description = "썸네일 URL (설명 이미지가 있으면 그 중 첫 번째, 없으면 대표/첫 STL)")
        private String thumbnailUrl;


        public static ModelResponse from(Model model, boolean purchaseAvailability, boolean downloadable) {

            java.util.List<String> stlUrls = (model.getAssets() != null && !model.getAssets().isEmpty())
                    ? model.getAssets().stream().map(a -> a.getFile().getUrl()).toList()
                    : java.util.List.of();

            java.util.List<String> descUrls = (model.getDescriptionImages() != null && !model.getDescriptionImages().isEmpty())
                    ? model.getDescriptionImages().stream().map(di -> di.getFile().getUrl()).toList()
                    : java.util.List.of();

            String thumb = null;
            if (!descUrls.isEmpty()) {
                thumb = descUrls.get(0); // 설명 이미지 최우선
            } else if (model.getPrimaryAsset() != null) {
                thumb = model.getPrimaryAsset().getFile().getUrl();
            } else if (!stlUrls.isEmpty()) {
                thumb = stlUrls.get(0);
            }

            List<String> urls = model.getAssets().stream()
                    .map(asset -> asset.getFile().getUrl())
                    .toList();

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
                    .purchaseAvailability(purchaseAvailability)
                    .category(model.getCategory())
                    .downloadable(downloadable)
                    .modelFileUrls(urls)
                    .descriptionImageUrls(descUrls)
                    .thumbnailUrl(thumb)
                    .build();
        }

        public static ModelResponse from(ModelPurchase modelPurchase, boolean purchaseAvailability) {

            Model model = modelPurchase.getModel();
            List<String> urls = model.getAssets().stream()
                    .map(asset -> asset.getFile().getUrl())
                    .toList();

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
                    .purchaseAvailability(purchaseAvailability)
                    .category(model.getCategory())
                    .paymentId(modelPurchase.getPaymentId())
                    .downloadable(model.isDownloadable())
                    .modelFileUrls(urls)
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
        @Schema(description = "모델 카테고리")
        private ModelCategory category;

        @Schema(description = "도면 다운로드 가능 여부", example = "true")
        private boolean downloadable;

        @io.swagger.v3.oas.annotations.media.ArraySchema(
                schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string", format = "binary"),
                minItems = 0, maxItems = 10
        )
        @Schema(description = "설명용 이미지 파일들 (최대 10개)")
        private org.springframework.web.multipart.MultipartFile[] descriptionImages;

        @io.swagger.v3.oas.annotations.media.ArraySchema(
                schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string", format = "binary"),
                minItems = 1, maxItems = 100
        )
        @Schema(description = "STL 파일들 (최대 100개)")
        private MultipartFile[] modelFiles;
    }
}
