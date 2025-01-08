package com.example.crafteria_server.domain.technology.repository;

import com.example.crafteria_server.domain.technology.entity.Technology;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechnologyRepository extends JpaRepository<Technology, Long> {
    List<Technology> findByManufacturerId(Long manufacturerId);
}
