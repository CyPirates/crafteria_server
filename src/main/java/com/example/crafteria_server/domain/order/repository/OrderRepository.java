package com.example.crafteria_server.domain.order.repository;

import com.example.crafteria_server.domain.chart.dto.*;
import com.example.crafteria_server.domain.order.entity.Order;
import com.example.crafteria_server.domain.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findAllByUserId(Long userId, Pageable pageable);

    Optional<Order> findByUserIdAndId(Long userId, Long orderId);

    Optional<Order> findByManufacturerIdAndId(Long manufacturerId, Long orderId);

    List<Order> findByManufacturerIdAndStatus(Long manufacturerId, OrderStatus status);

    List<Order> findByManufacturerId(Long manufacturerId);

    List<Order> findAllByManufacturerId(Long manufacturerId);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status != 'ORDERED'")
    List<Order> findAllByUserIdExcludingOrdered(Long userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.manufacturer.id = :manufacturerId AND o.status != 'ORDERED'")
    List<Order> findAllByManufacturerIdExcludingOrdered(Long manufacturerId, Pageable pageable);

    List<Order> findByUserIdAndManufacturerIdAndStatus(Long userId, Long manufacturerId, OrderStatus status);
    // 특정일 매출 총액 및 건수
    @Query("SELECT CAST(o.createDate AS date) AS salesDate, " +
            "SUM(o.purchasePrice) AS totalSalesAmount, " +
            "COUNT(o) AS totalOrders " +
            "FROM Order o " +
            "WHERE o.manufacturer.id = :manufacturerId " +
            "AND CAST(o.createDate AS date) = :specificDate " +
            "AND o.status != 'CANCELED' " +
            "GROUP BY CAST(o.createDate AS date)")
    SalesStatisticsProjection findSalesStatisticsByDate(
            @Param("manufacturerId") Long manufacturerId,
            @Param("specificDate") LocalDate specificDate
    );

    // 특정 기간 매출 총액 및 건수
    @Query("SELECT SUM(o.purchasePrice) AS totalSalesAmount, COUNT(o) AS totalOrders " +
            "FROM Order o " +
            "WHERE o.manufacturer.id = :manufacturerId " +
            "AND o.createDate >= :startDateTime " +
            "AND o.createDate <= :endDateTime " +
            "AND o.status != 'CANCELED' ")
    PeriodSalesStatisticsProjection findSalesStatisticsByPeriod(
            @Param("manufacturerId") Long manufacturerId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    // 월별 매출 총액 및 건수
    @Query("SELECT YEAR(o.createDate) AS year, MONTH(o.createDate) AS month, " +
            "SUM(o.purchasePrice) AS totalSalesAmount, COUNT(o) AS totalOrders " +
            "FROM Order o " +
            "WHERE o.manufacturer.id = :manufacturerId " +
            "AND o.createDate BETWEEN :startDate AND :endDate " +
            "AND o.status != 'CANCELED' " +
            "GROUP BY YEAR(o.createDate), MONTH(o.createDate) " +
            "ORDER BY YEAR(o.createDate), MONTH(o.createDate)")
    List<MonthlySalesStatisticsProjection> findMonthlySalesStatistics(
            @Param("manufacturerId") Long manufacturerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // 주별 매출 통계 (네이티브 쿼리)
    @Query(value = """
    SELECT 
        YEAR(o.created_at) AS year,
        WEEK(o.created_at, 1) AS week,
        SUM(o.total_price) AS totalSalesAmount,
        COUNT(o.id) AS totalOrders
    FROM orders o
    WHERE o.manufacturer_id = :manufacturerId
      AND o.status = 'PAID'
      AND o.created_at BETWEEN :startDate AND :endDate
    GROUP BY year, week
    ORDER BY year, week
    """, nativeQuery = true)
    List<WeeklySalesStatisticsProjection> findWeeklySalesStatistics(@Param("manufacturerId") Long manufacturerId,
                                                                    @Param("startDate") LocalDate startDate,
                                                                    @Param("endDate") LocalDate endDate);

    List<Order> findAllByUserIdAndStatusNotOrderByCreateDateDesc(
            Long userId,
            OrderStatus status,
            Pageable pageable
    );

}


