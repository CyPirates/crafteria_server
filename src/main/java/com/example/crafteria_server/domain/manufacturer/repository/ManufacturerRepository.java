package com.example.crafteria_server.domain.manufacturer.repository;

import com.example.crafteria_server.domain.manufacturer.entity.Manufacturer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ManufacturerRepository extends JpaRepository<Manufacturer, Long> {
    List<Manufacturer> findByNameContainingOrIntroductionContaining(String name, String introduction);
    List<Manufacturer> findByNameContaining(String name);
    List<Manufacturer> findByIntroductionContaining(String introduction);
}