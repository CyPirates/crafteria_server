package com.example.crafteria_server.domain.manufacturer.repository;

import com.example.crafteria_server.domain.manufacturer.entity.Manufacturer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManufacturerRepository extends JpaRepository<Manufacturer, Long> {
}