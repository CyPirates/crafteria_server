package com.example.crafteria_server.domain.order.repository;

import com.example.crafteria_server.domain.order.entity.Order;
import com.example.crafteria_server.domain.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findAllByUserId(Long userId, Pageable pageable);
    Optional<Order> findByUserIdAndId(Long userId, Long orderId);
    Optional<Order> findByManufacturerIdAndId(Long manufacturerId, Long orderId);
    List<Order> findByManufacturerIdAndStatus(Long manufacturerId, OrderStatus status);
    List<Order> findByManufacturerId(Long manufacturerId);
    Optional<Order> findByUserIdAndManufacturerIdAndStatus(Long userId, Long manufacturerId, OrderStatus status);
}
