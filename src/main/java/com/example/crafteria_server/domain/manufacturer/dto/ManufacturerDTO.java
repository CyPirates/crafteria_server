package com.example.crafteria_server.domain.manufacturer.dto;


import com.example.crafteria_server.domain.equipment.dto.EquipmentDto;
import com.example.crafteria_server.domain.manufacturer.entity.Manufacturer;
import com.example.crafteria_server.domain.technology.dto.TechnologyDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ManufacturerDTO {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ManufacturerResponse {
        @NotNull
        @Schema(description = "제조사 ID", example = "1")
        private Long id;

        @NotNull
        @Schema(description = "제조사명", example = "삼성전자")
        private String name;

        @NotNull
        @Schema(description = "제조사 소개", example = "삼성전자는 전자제품을 만드는 회사입니다.")
        private String introduction;

        @NotNull
        @Schema(description = "제조사 주소", example = "서울시 강남구 역삼동 123-456")
        private String address;

        @NotNull
        @Schema(description = "제조사 전화번호", example = "02-1234-5678")
        private String dialNumber;

        @NotNull
        @Schema(description = "제조 횟수", example = "100")
        private Integer productionCount;

        @NotNull
        @Schema(description = "제조사 평점", example = "5")
        private Integer rating;

        @NotNull
        @Schema(description = "대표 장비", example = "스마트폰")
        private String representativeEquipment;

        @NotNull
        @Schema(description = "모델 파일 URL", example = "http://localhost:8080/image/1")
        private String imageFileUrl;

        @Schema(description = "장비 리스트")
        private List<EquipmentDto.EquipmentResponse> equipmentList;

        @NotNull
        @Schema(description = "단위 가격", example = "1200.50")
        private BigDecimal unitPrice;  // **단위 가격 추가**

        @NotNull
        @Schema(description = "총 리뷰 수", example = "100")
        private Integer totalReviews;  // **총 리뷰 수 추가**

        @Schema(description = "기술 목록")
        private List<TechnologyDto.TechnologyResponse> technologies;

        public static ManufacturerResponse from(Manufacturer manufacturer) {
            return ManufacturerResponse.builder()
                    .id(manufacturer.getId())
                    .name(manufacturer.getName())
                    .introduction(manufacturer.getIntroduction())
                    .address(manufacturer.getAddress())
                    .dialNumber(manufacturer.getDialNumber())
                    .productionCount(manufacturer.getProductionCount())
                    .rating(manufacturer.getRating())
                    .representativeEquipment(manufacturer.getRepresentativeEquipment())
                    .imageFileUrl(manufacturer.getImage().getUrl())
                    .unitPrice(manufacturer.getUnitPrice())
                    .totalReviews(manufacturer.getTotalReviews())// **단위 가격 추가**
                    .equipmentList(manufacturer.getEquipmentList() != null ?
                            manufacturer.getEquipmentList().stream()
                                    .map(EquipmentDto.EquipmentResponse::from)
                                    .collect(Collectors.toList())
                            : Collections.emptyList())
                    .technologies(manufacturer.getTechnologies().stream()
                            .map(TechnologyDto.TechnologyResponse::from)
                            .collect(Collectors.toList()))
                    .build();
        }

    }
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "제조사 등록 요청", description = "제조사 등록 요청", type = "multipartForm")
    public static class ManufacturerRequest {
        @NotNull
        @Schema(description = "제조사명", example = "삼성프린트")
        private String name;

        @NotNull
        @Schema(description = "제조사 소개", example = "삼성프린트는 3D프린팅을 하는 회사입니다.")
        private String introduction;

        @NotNull
        @Schema(description = "제조사 주소", example = "서울시 강남구 역삼동 123-456")
        private String address;

        @NotNull
        @Schema(description = "제조사 전화번호", example = "02-1234-5678")
        private String dialNumber;


        @NotNull
        @Schema(description = "대표 장비", example = "creality 3D")
        private String representativeEquipment;

        @NotNull
        @Schema(description = "대표 이미지", example = "image.jpg")
        private MultipartFile image;

        @NotNull
        @Schema(description = "단위 가격", example = "1200.50")
        private BigDecimal unitPrice; // **단위 가격 추가**

    }
}