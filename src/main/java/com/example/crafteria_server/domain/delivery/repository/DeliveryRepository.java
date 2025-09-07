package com.example.crafteria_server.domain.delivery.repository;

import com.example.crafteria_server.domain.delivery.entity.Delivery;
import com.example.crafteria_server.domain.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    List<Delivery> findAllByOrder_Manufacturer_Id(Long manufacturerId);
    List<Delivery> findAllByOrder_User_Id(Long userId);
    Optional<Delivery> findByTrackingNumber(String trackingNumber);
    List<Delivery> findAllByTrackingNumberNotNullAndOrder_StatusNot(OrderStatus status);
}
