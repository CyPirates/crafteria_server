package com.example.crafteria_server.domain.ads.dto;

import com.example.crafteria_server.domain.ads.entity.AdvertisementImage;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdvertisementImageDto {
    private Long id;           // 광고 이미지 ID
    private String title;      // 광고 제목
    private String description; // 광고 설명
    private String imageUrl;    // 광고 이미지 URL

    public static AdvertisementImageDto from(AdvertisementImage advertisementImage) {
        return AdvertisementImageDto.builder()
                .id(advertisementImage.getId())
                .title(advertisementImage.getTitle())
                .description(advertisementImage.getDescription())
                .imageUrl(advertisementImage.getFile().getUrl()) // 파일 URL만 반환
                .build();
    }
}
