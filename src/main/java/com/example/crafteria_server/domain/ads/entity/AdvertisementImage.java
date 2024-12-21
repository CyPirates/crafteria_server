package com.example.crafteria_server.domain.ads.entity;

import com.example.crafteria_server.domain.file.entity.File;
import com.example.crafteria_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "advertisement_image")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AdvertisementImage extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "file_id", nullable = true)
    private File file; // 업로드된 파일 정보

    @Column(name = "title", nullable = false)
    private String title; // 광고 이미지 제목

    @Column(name = "description", length = 1000)
    private String description; // 광고 이미지 설명
}
