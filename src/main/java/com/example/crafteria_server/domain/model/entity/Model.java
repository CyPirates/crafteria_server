package com.example.crafteria_server.domain.model.entity;

import com.example.crafteria_server.domain.file.entity.File;
import com.example.crafteria_server.domain.user.entity.Author;
import com.example.crafteria_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "model")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Model extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    @Column(nullable = false)
    private String name;

    @Column()
    private String description;

    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private java.util.List<ModelDescriptionImage> descriptionImages = new java.util.ArrayList<>();

    // rating: 0 ~ 10
    @Column(nullable = false)
    @Builder.Default
    private int rating = 5;

    @Column(nullable = false)
    private long price;

    @Column(nullable = false)
    @Builder.Default
    private long viewCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private long downloadCount = 0;

    @Column(nullable = false)
    private double widthSize;

    @Column(nullable = false)
    private double lengthSize;

    @Column(nullable = false)
    private double heightSize;

    @Column(nullable = false)
    private double magnification;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "model_file_id")
    private File modelFile;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ModelCategory category;

    @Column(nullable = false)
    @Builder.Default
    private boolean isDeleted = false; // 🔥 Soft Delete 필드 추가

    @Column(nullable = false)
    @Builder.Default
    private boolean isDownloadable = true;

    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<ModelAsset> assets = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_asset_id")
    private ModelAsset primaryAsset;

}
