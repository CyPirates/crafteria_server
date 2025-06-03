package com.example.crafteria_server.domain.delivery.repository;

import com.example.crafteria_server.domain.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    List<Delivery> findAllByOrder_Manufacturer_Id(Long manufacturerId);
}
