package com.example.crafteria_server.domain.equipment.repository;

import com.example.crafteria_server.domain.equipment.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    // 특정 제조사의 장비들을 조회하는 메서드
    List<Equipment> findByManufacturerId(Long manufacturerId);
}
