package com.example.crafteria_server.domain.technology.dto;

import com.example.crafteria_server.domain.technology.entity.Material;
import com.example.crafteria_server.domain.technology.entity.Technology;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public class TechnologyDto {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TechnologyRequest {
        @NotNull
        @Schema(description = "제조사 ID", example = "1")
        private Long manufacturerId;
        @NotNull
        @Schema(description = "재료")
        private Material material;
        @NotNull
        @Schema(description = "설명", example = "설명")
        private String description;
        @NotNull
        @Schema(description = "색상값", example = "#FFFFFF")
        private String colorValue;
        @NotNull
        @Schema(description = "이미지 파일", example = "image.jpg")
        private MultipartFile imageFile;
        @NotNull
        @Schema(description = "시간당 가격", example = "1000")
        private BigDecimal pricePerHour;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TechnologyResponse {
        @Schema(description = "기술 ID", example = "1")
        private Long technologyId;
        @Schema(description = "제조사 ID", example = "1")
        private Long manufacturerId;
        @Schema(description = "재료", example = "FILAMENT,POWDER, LIQUID")
        private Material material;
        @Schema(description = "설명", example = "설명")
        private String description;
        @Schema(description = "색상값", example = "#FFFFFF")
        private String colorValue;
        @Schema(description = "이미지 URL", example = "http://example.com")
        private String imageUrl;
        @Schema(description = "시간당 가격", example = "1000")
        private BigDecimal pricePerHour;

        public static TechnologyResponse from(Technology technology) {
            return TechnologyResponse.builder()
                    .technologyId(technology.getId())
                    .manufacturerId(technology.getManufacturer().getId())
                    .material(technology.getMaterial())
                    .description(technology.getDescription())
                    .colorValue(technology.getColorValue())
                    .imageUrl(technology.getImage() != null ? technology.getImage().getUrl() : null)
                    .pricePerHour(technology.getPricePerHour())
                    .build();
        }
    }
}
