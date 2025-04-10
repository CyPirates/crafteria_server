package com.example.crafteria_server.domain.ads.dto;

import com.example.crafteria_server.domain.ads.entity.AdvertisementImage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdvertisementImageDto {
    @Schema(description = "광고 이미지 ID", example = "1")
    private Long id;           // 광고 이미지 ID

    @Schema(description = "광고 제목", example = "광고 제목")
    private String title;      // 광고 제목

    @Schema(description = "광고 url", example = "https://crafteria.com/ads/1")
    private String linkurl; // 광고 설명

    @Schema(description = "광고 이미지 url", example = "https://crafteria.com/ads/1")
    private String imageUrl;    // 광고 이미지 URL

    public static AdvertisementImageDto from(AdvertisementImage advertisementImage) {
        return AdvertisementImageDto.builder()
                .id(advertisementImage.getId())
                .title(advertisementImage.getTitle())
                .linkurl(advertisementImage.getLinkurl())
                .imageUrl(advertisementImage.getFile().getUrl()) // 파일 URL만 반환
                .build();
    }
}
