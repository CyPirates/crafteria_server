package com.example.crafteria_server.domain.model.repository;

import com.example.crafteria_server.domain.model.entity.ModelPurchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModelPurchaseRepository extends JpaRepository<ModelPurchase, Long> {
    Page<ModelPurchase> findAllByUserIdOrderByCreateDateDesc(Long userId, Pageable pageable);
    Optional<ModelPurchase> findByUserIdAndModelId(Long userId, Long modelId);
    boolean existsByPaymentId(String paymentId);
    Page<ModelPurchase> findAllByUserIdAndVerifiedTrueOrderByCreateDateDesc(Long userId, Pageable pageable);
}
