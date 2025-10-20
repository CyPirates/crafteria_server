package com.example.crafteria_server.domain.model.repository;

import com.example.crafteria_server.domain.chart.dto.UserMonthlySalesStatisticsProjection;
import com.example.crafteria_server.domain.chart.dto.UserSalesStatisticsProjection;
import com.example.crafteria_server.domain.model.entity.ModelPurchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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

    // 특정 날짜별 매출
    @Query("""
        SELECT DATE(mp.createDate) AS salesDate,
               SUM(m.price) AS totalSalesAmount,
               COUNT(*) AS totalOrders
        FROM ModelPurchase mp
        JOIN mp.model m
        WHERE m.author.id = :authorId
          AND mp.verified = true
          AND DATE(mp.createDate) = :date
        GROUP BY salesDate
    """)
    UserSalesStatisticsProjection findDailySales(@Param("authorId") Long authorId, @Param("date") LocalDate date);

    // 특정 기간 매출
    @Query("""
        SELECT SUM(m.price) AS totalSalesAmount,
               COUNT(*) AS totalOrders
        FROM ModelPurchase mp
        JOIN mp.model m
        WHERE m.author.id = :authorId
          AND mp.verified = true
          AND DATE(mp.createDate) BETWEEN :startDate AND :endDate
    """)
    UserSalesStatisticsProjection findPeriodSales(@Param("authorId") Long authorId,
                                                  @Param("startDate") LocalDate start,
                                                  @Param("endDate") LocalDate end);

    // 월별 매출
    @Query("""
    SELECT YEAR(mp.createDate) AS year,
           MONTH(mp.createDate) AS month,
           SUM(m.price) AS totalSalesAmount,
           COUNT(*) AS totalOrders
    FROM ModelPurchase mp
    JOIN mp.model m
    WHERE m.author.id = :authorId
      AND mp.verified = true
      AND DATE(mp.createDate) BETWEEN :startDate AND :endDate
    GROUP BY YEAR(mp.createDate), MONTH(mp.createDate)
    ORDER BY YEAR(mp.createDate), MONTH(mp.createDate)
""")
    List<UserMonthlySalesStatisticsProjection> findMonthlySales(
            @Param("authorId") Long authorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // 주간 매출 (일요일~토요일로 계산 가능)
    @Query("""
    SELECT DATE_FORMAT(mp.createDate, '%x-%v') AS week,
           SUM(m.price) AS totalSalesAmount,
           COUNT(*) AS totalOrders
    FROM ModelPurchase mp
    JOIN mp.model m
    WHERE m.author.id = :authorId
      AND mp.verified = true
      AND DATE(mp.createDate) BETWEEN :startDate AND :endDate
    GROUP BY DATE_FORMAT(mp.createDate, '%x-%v')
    ORDER BY DATE_FORMAT(mp.createDate, '%x-%v')
""")
    List<Object[]> findWeeklySales(
            @Param("authorId") Long authorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
