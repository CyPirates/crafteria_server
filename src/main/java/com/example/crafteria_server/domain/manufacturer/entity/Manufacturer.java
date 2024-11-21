package com.example.crafteria_server.domain.manufacturer.entity;

import com.example.crafteria_server.domain.equipment.entity.Equipment;
import com.example.crafteria_server.domain.file.entity.File;
import com.example.crafteria_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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
    private Integer productionCount = 0;  // 제작 횟수, 기본값 0

    @Column(name = "rating", nullable = false)
    private Integer rating = 0;  // 평점, 기본값 0

    @Column(name = "representative_equipment", length = 255)
    private String representativeEquipment;  // 대표 장비, VARCHAR(255)

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "image")
    private File image;  // 대표 이미지, FK, BIGINT

    @OneToMany(mappedBy = "manufacturer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Equipment> equipmentList = new ArrayList<>();  // 제조사가 보유한 장비 리스트
}
