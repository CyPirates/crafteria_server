package com.example.crafteria_server.domain.model.repository;

import com.example.crafteria_server.domain.model.entity.ModelPurchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModelPurchaseRepository extends JpaRepository<ModelPurchase, Long> {
    Page<ModelPurchase> findAllByUserIdOrderByCreateDateDesc(Long userId, Pageable pageable);
    Optional<ModelPurchase> findByUserIdAndModelId(Long userId, Long modelId);
    boolean existsByPaymentId(String paymentId);
    Page<ModelPurchase> findAllByUserIdAndVerifiedTrueOrderByCreateDateDesc(Long userId, Pageable pageable);

    Optional<ModelPurchase> findByPaymentId(String paymentId);

    Optional<Object> findByUserIdAndModelIdAndVerifiedTrue(Long userId, Long modelId);

    @Query("""
    SELECT mp
    FROM ModelPurchase mp
    JOIN FETCH mp.model m
    JOIN FETCH mp.user u
    WHERE mp.verified = true
      AND m.author.id = :authorId
    ORDER BY mp.createDate DESC
""")
    List<ModelPurchase> findVerifiedPurchasesByModelAuthor(@Param("authorId") Long authorId);
}
