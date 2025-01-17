package com.example.crafteria_server.domain.manufacturer.entity;

import com.example.crafteria_server.domain.equipment.entity.Equipment;
import com.example.crafteria_server.domain.file.entity.File;
import com.example.crafteria_server.domain.technology.entity.Technology;
import com.example.crafteria_server.domain.user.entity.User;
import com.example.crafteria_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "manufacturer")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Manufacturer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;  // PK, BIGINT

    @Column(name = "name", nullable = false, length = 50)
    private String name;  // 이름, VARCHAR(50)

    @Column(name = "introduction", length = 4000)
    private String introduction;  // 소개, VARCHAR(4000)

    @Column(name = "address", length = 255)
    private String address;  // 주소, VARCHAR(255)

    @Column(name = "dial_number", length = 255)
    private String dialNumber;  // 전화번호, VARCHAR(255)

    @Column(name = "production_count", nullable = false)
    @Builder.Default
    private Integer productionCount = 0;  // 제작 횟수, 기본값 0

    @Column(name = "rating", nullable = false)
    @Builder.Default
    private Integer rating = 0;  // 평점, 기본값 0

    @Column(name = "representative_equipment", length = 255)
    private String representativeEquipment;  // 대표 장비, VARCHAR(255)

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "image")
    private File image;  // 대표 이미지, FK, BIGINT

    @OneToMany(mappedBy = "manufacturer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Equipment> equipmentList = new ArrayList<>();  // 제조사가 보유한 장비 리스트

    // 대시보드 계정과 1대1 매핑
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dashboard_user_id", nullable = false)
    private User dashboardUser;  // 대시보드 계정

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;  // **단위 가격 추가**

    @Column(name = "total_reviews", nullable = false)
    @Builder.Default
    private Integer totalReviews = 0;  // 총 리뷰 수, 기본값 0

    @OneToMany(mappedBy = "manufacturer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Technology> technologies = new ArrayList<>();
}
