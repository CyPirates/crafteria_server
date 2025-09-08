package com.example.crafteria_server.domain.review.repository;

import com.example.crafteria_server.domain.manufacturer.entity.Manufacturer;
import com.example.crafteria_server.domain.model.entity.Model;
import com.example.crafteria_server.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByManufacturer(Manufacturer manufacturer);
    Page<Review> findByManufacturerId(Long manufacturerId, Pageable pageable);

    boolean existsByUser_IdAndOrder_Id(Long userId, Long orderId);
    Optional<Review> findByUser_IdAndOrder_Id(Long userId, Long orderId);

    List<Review> findByUser_IdAndOrder_IdIn(Long userId, List<Long> orderIds);
}
