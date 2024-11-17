package com.example.crafteria_server.domain.equipment.dto;

import com.example.crafteria_server.domain.equipment.entity.Equipment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

public class EquipmentDto {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EquipmentResponse {
        @NotNull
        @Schema(description = "장비 ID", example = "1")
        private Long id;

        @NotNull
        @Schema(description = "장비명", example = "creality 3D")
        private String name;

        @Schema(description = "장비 설명", example = "3D 프린터")
        private String description;

        @NotNull
        @Schema(description = "장비 상태", example = "출력중")
        private String status;

        @Schema(description = "장비 이미지 URL", example = "http://localhost:8080/image/1")
        private String imageFileUrl;

        @NotNull
        @Schema(description = "제조사 id", example = "1")
        private Long manufacturerId;

        public static EquipmentResponse from(Equipment equipment) {
            return EquipmentResponse.builder()
                    .id(equipment.getId())
                    .name(equipment.getName())
                    .status(equipment.getStatus().name())
                    .imageFileUrl(equipment.getImage().getUrl())
                    .manufacturerId(equipment.getManufacturer().getId())
                    .build();
        }
    }
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "장비 등록 요청", description = "장비 등록 요청", type = "multipartForm")
    public static class EquipmentRequest {
        @NotNull
        @Schema(description = "장비명", example = "creality 3D")
        private String name;

        @NotNull
        @Schema(description = "장비 이미지", example = "image.jpg")
        private MultipartFile image;

        @Schema(description = "장비 설명", example = "3D 프린터")
        private String description;

        @NotNull
        @Schema(description = "제조사 id", example = "1")
        private Long manufacturerId;

    }
}
