package com.example.crafteria_server.domain.user.dto;

import com.example.crafteria_server.domain.user.entity.UserAddress;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;


public class UserAddressDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserAddressRequest {
        @Schema(description = "주소 이름", example = "집")
        private String label;

        @Schema(description = "기본 주소", example = "서울특별시 강남구 역삼동 123-45")
        private String baseAddress;

        @Schema(description = "상세 주소", example = "아파트 101호")
        private String detailAddress;

        @Schema(description = "기본 주소 여부", example = "true")
        private boolean isDefault;

        @Schema(description = "우편번호(5자리)", example = "06236")
        private String postalCode;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserAddressResponse {

        @Schema(description = "주소 ID", example = "1")
        private Long id;

        @Schema(description = "주소 이름", example = "집")
        private String label;

        @Schema(description = "기본 주소", example = "서울특별시 강남구 역삼동 123-45")
        private String baseAddress;

        @Schema(description = "상세 주소", example = "아파트 101호")
        private String detailAddress;

        @Schema(description = "기본 주소 여부", example = "true")
        private boolean isDefault;

        @Schema(description = "우편번호(5자리)", example = "06236")
        private String postalCode;

        public static UserAddressResponse from(UserAddress address) {
            return UserAddressResponse.builder()
                    .id(address.getId())
                    .label(address.getLabel())
                    .baseAddress(address.getBaseAddress())
                    .detailAddress(address.getDetailAddress())
                    .isDefault(address.isDefault())
                    .postalCode(address.getPostalCode())
                    .build();
        }


    }


}
